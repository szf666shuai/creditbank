package com.creditbank.platform.module.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.ActivityInvitationVO;
import com.creditbank.platform.module.enterprise.dto.SendActivityInviteRequest;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.ActivityInvitation;
import com.creditbank.platform.module.enterprise.entity.ActivityRegistration;
import com.creditbank.platform.module.enterprise.mapper.ActivityInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.ActivityRegistrationMapper;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EnterpriseActivityInvitationService {

    private static final int MSG_TYPE_ACTIVITY = 3;
    private static final String REF_TYPE_ACTIVITY = "activity";
    private static final int UNREAD = 0;
    private static final int ACTIVITY_CANCELLED = 0;
    private static final int ACTIVITY_OPEN = 1;
    private static final int ACTIVITY_ONGOING = 2;
    private static final int ACTIVITY_ENDED = 3;
    private static final int INVITE_PENDING = 0;
    private static final int REG_REGISTERED = 0;
    private static final int REG_CHECKED_IN = 1;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final ActivityMapper activityMapper;
    private final ActivityInvitationMapper activityInvitationMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final UserMessageMapper userMessageMapper;

    public List<ActivityInvitationVO> listSentInvitations() {
        SysUser user = authSupport.requireEnterprise();
        List<Activity> activities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrgId, user.getOrgId()));
        if (activities.isEmpty()) {
            return List.of();
        }
        List<Long> activityIds = activities.stream().map(Activity::getId).toList();
        List<ActivityInvitation> invitations = activityInvitationMapper.selectList(
                new LambdaQueryWrapper<ActivityInvitation>()
                        .in(ActivityInvitation::getActivityId, activityIds)
                        .orderByDesc(ActivityInvitation::getCreateTime));
        return toVOList(invitations, activities);
    }

    @Transactional
    public ActivityInvitationVO sendInvitation(SendActivityInviteRequest request) {
        SysUser sender = authSupport.requireEnterpriseWritable();
        Long orgId = sender.getOrgId();
        Long toUserId = request.getToUserId();

        if (Objects.equals(sender.getId(), toUserId)) {
            throw new BusinessException(400, "不能向自己发送活动邀请");
        }

        validateStudentUser(toUserId);
        Activity activity = authSupport.requireOrgActivity(request.getActivityId(), orgId);
        validateActivityInvitable(activity);
        ensureNoPendingInvite(activity.getId(), toUserId);
        ensureNotRegistered(activity.getId(), toUserId);
        ensureCapacityAvailable(activity);

        SysOrganization org = orgMapper.selectById(orgId);
        String orgName = org != null ? org.getName() : "企业";

        UserMessage message = new UserMessage();
        message.setFromUserId(sender.getId());
        message.setToUserId(toUserId);
        message.setMsgType(MSG_TYPE_ACTIVITY);
        message.setTitle("活动邀请：" + activity.getTitle());
        message.setContent(buildMessageContent(orgName, activity, request.getRemark()));
        message.setRefType(REF_TYPE_ACTIVITY);
        message.setReadStatus(UNREAD);
        userMessageMapper.insert(message);

        ActivityInvitation invitation = new ActivityInvitation();
        invitation.setActivityId(activity.getId());
        invitation.setFromUserId(sender.getId());
        invitation.setToUserId(toUserId);
        invitation.setMessageId(message.getId());
        invitation.setStatus(INVITE_PENDING);
        activityInvitationMapper.insert(invitation);

        message.setRefId(invitation.getId());
        userMessageMapper.updateById(message);

        ActivityInvitation saved = activityInvitationMapper.selectById(invitation.getId());
        return toVO(saved, activity, orgName, loadNameMap(saved));
    }

    private void validateActivityInvitable(Activity activity) {
        Integer status = activity.getStatus();
        if (status == null || status == ACTIVITY_CANCELLED || status == ACTIVITY_ENDED) {
            throw new BusinessException(400, "仅报名中或进行中的活动可发送邀请");
        }
        if (status != ACTIVITY_OPEN && status != ACTIVITY_ONGOING) {
            throw new BusinessException(400, "当前活动状态不可邀请");
        }
    }

    private void ensureNoPendingInvite(Long activityId, Long toUserId) {
        Long count = activityInvitationMapper.selectCount(new LambdaQueryWrapper<ActivityInvitation>()
                .eq(ActivityInvitation::getActivityId, activityId)
                .eq(ActivityInvitation::getToUserId, toUserId)
                .eq(ActivityInvitation::getStatus, INVITE_PENDING));
        if (count != null && count > 0) {
            throw new BusinessException(400, "该学员已有待回复的活动邀请");
        }
    }

    private void ensureNotRegistered(Long activityId, Long toUserId) {
        Long count = activityRegistrationMapper.selectCount(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getUserId, toUserId)
                .in(ActivityRegistration::getStatus, REG_REGISTERED, REG_CHECKED_IN));
        if (count != null && count > 0) {
            throw new BusinessException(400, "该学员已报名此活动");
        }
    }

    private void ensureCapacityAvailable(Activity activity) {
        if (activity.getMaxParticipants() == null || activity.getMaxParticipants() <= 0) {
            return;
        }
        long registered = countActiveRegistrations(activity.getId());
        if (registered >= activity.getMaxParticipants()) {
            throw new BusinessException(400, "活动报名人数已满");
        }
    }

    private long countActiveRegistrations(Long activityId) {
        Long count = activityRegistrationMapper.selectCount(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .in(ActivityRegistration::getStatus, REG_REGISTERED, REG_CHECKED_IN));
        return count != null ? count : 0;
    }

    private String buildMessageContent(String orgName, Activity activity, String remark) {
        StringBuilder sb = new StringBuilder();
        sb.append("您好，").append(orgName).append(" 邀请您参加活动「").append(activity.getTitle()).append("」。\n\n");
        if (activity.getStartTime() != null) {
            sb.append("开始时间：").append(activity.getStartTime().format(TIME_FORMAT)).append('\n');
        }
        if (activity.getEndTime() != null) {
            sb.append("结束时间：").append(activity.getEndTime().format(TIME_FORMAT)).append('\n');
        }
        if (StringUtils.hasText(activity.getLocation())) {
            sb.append("活动地点：").append(activity.getLocation()).append('\n');
        }
        if (activity.getCreditReward() != null && activity.getCreditReward().signum() > 0) {
            sb.append("参与奖励：").append(activity.getCreditReward()).append(" 学分\n");
        }
        sb.append('\n');
        if (StringUtils.hasText(remark)) {
            sb.append(remark.trim()).append("\n\n");
        }
        sb.append("您也可在企业详情页主动报名。若收到本邀请，请在「活动邀请」页面接受或拒绝；接受后将自动完成报名。");
        return sb.toString();
    }

    private void validateStudentUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(400, "受邀人必须是学员用户");
        }
    }

    private List<ActivityInvitationVO> toVOList(List<ActivityInvitation> invitations, List<Activity> activities) {
        if (invitations.isEmpty()) {
            return List.of();
        }
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, a -> a, (a, b) -> a));
        Map<Long, String> orgNameMap = orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                        .in(SysOrganization::getId, activities.stream().map(Activity::getOrgId).distinct().toList()))
                .stream()
                .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));
        Map<Long, String> nameMap = loadNameMap(invitations);
        return invitations.stream()
                .map(item -> {
                    Activity activity = activityMap.get(item.getActivityId());
                    String orgName = activity != null ? orgNameMap.get(activity.getOrgId()) : null;
                    return toVO(item, activity, orgName, nameMap);
                })
                .toList();
    }

    private ActivityInvitationVO toVO(ActivityInvitation invitation, Activity activity, String orgName,
                                      Map<Long, String> nameMap) {
        return ActivityInvitationVO.builder()
                .id(invitation.getId())
                .activityId(invitation.getActivityId())
                .activityTitle(activity != null ? activity.getTitle() : null)
                .orgId(activity != null ? activity.getOrgId() : null)
                .orgName(orgName)
                .location(activity != null ? activity.getLocation() : null)
                .startTime(activity != null ? activity.getStartTime() : null)
                .endTime(activity != null ? activity.getEndTime() : null)
                .creditReward(activity != null ? activity.getCreditReward() : null)
                .fromUserId(invitation.getFromUserId())
                .fromUserName(nameMap.get(invitation.getFromUserId()))
                .toUserId(invitation.getToUserId())
                .toUserName(nameMap.get(invitation.getToUserId()))
                .messageId(invitation.getMessageId())
                .status(invitation.getStatus())
                .statusName(EnterpriseInterviewService.inviteStatusName(invitation.getStatus()))
                .createTime(invitation.getCreateTime())
                .build();
    }

    private Map<Long, String> loadNameMap(ActivityInvitation invitation) {
        return loadNameMap(List.of(invitation));
    }

    private Map<Long, String> loadNameMap(List<ActivityInvitation> invitations) {
        List<Long> userIds = invitations.stream()
                .flatMap(item -> Stream.of(item.getFromUserId(), item.getToUserId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)).stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        user -> StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()
                ));
    }
}
