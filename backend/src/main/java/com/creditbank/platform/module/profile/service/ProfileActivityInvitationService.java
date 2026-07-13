package com.creditbank.platform.module.profile.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.dto.ActivityInvitationVO;
import com.creditbank.platform.module.enterprise.service.ActivityLifecycleService;
import com.creditbank.platform.module.enterprise.support.ActivityStatus;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.ActivityInvitation;
import com.creditbank.platform.module.enterprise.entity.ActivityRegistration;
import com.creditbank.platform.module.enterprise.mapper.ActivityInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.ActivityRegistrationMapper;
import com.creditbank.platform.module.enterprise.service.EnterpriseInterviewService;
import com.creditbank.platform.security.AuthSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProfileActivityInvitationService {

    private static final int ACTIVITY_CANCELLED = ActivityStatus.CANCELLED;
    private static final int ACTIVITY_OPEN = ActivityStatus.OPEN;
    private static final int ACTIVITY_ONGOING = ActivityStatus.ONGOING;
    private static final int ACTIVITY_ENDED = ActivityStatus.ENDED;
    private static final int INVITE_PENDING = 0;
    private static final int INVITE_ACCEPTED = 1;
    private static final int INVITE_REJECTED = 2;
    private static final int REG_REGISTERED = 0;
    private static final int REG_CHECKED_IN = 1;
    private static final int REG_REJECTED_INVITE = 3;

    private final AuthSupport authSupport;
    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final ActivityMapper activityMapper;
    private final ActivityInvitationMapper activityInvitationMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final ActivityLifecycleService activityLifecycleService;

    public List<ActivityInvitationVO> listMyInvitations() {
        SysUser user = authSupport.requireStudent();
        List<ActivityInvitation> invitations = activityInvitationMapper.selectList(
                new LambdaQueryWrapper<ActivityInvitation>()
                        .eq(ActivityInvitation::getToUserId, user.getId())
                        .orderByDesc(ActivityInvitation::getCreateTime));
        return toVOList(invitations);
    }

    public ActivityInvitationVO getMyInvitation(Long invitationId) {
        SysUser user = authSupport.requireStudent();
        ActivityInvitation invitation = getOwnedInvitationOrThrow(invitationId, user.getId());
        return toVO(invitation);
    }

    @Transactional
    public ActivityInvitationVO acceptInvitation(Long invitationId) {
        SysUser user = authSupport.requireStudent();
        ActivityInvitation invitation = getOwnedInvitationOrThrow(invitationId, user.getId());
        if (invitation.getStatus() == null || invitation.getStatus() != INVITE_PENDING) {
            throw new BusinessException(400, "该邀请已处理，无法重复操作");
        }

        Activity activity = activityLifecycleService.refreshById(invitation.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() == null
                || activity.getStatus() == ACTIVITY_CANCELLED
                || activity.getStatus() == ACTIVITY_ENDED
                || (activity.getStatus() != ACTIVITY_OPEN && activity.getStatus() != ACTIVITY_ONGOING)) {
            throw new BusinessException(400, "活动当前不可报名");
        }

        ActivityRegistration existing = activityRegistrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activity.getId())
                        .eq(ActivityRegistration::getUserId, user.getId()));
        if (existing != null && (existing.getStatus() == REG_REGISTERED || existing.getStatus() == REG_CHECKED_IN)) {
            throw new BusinessException(400, "您已报名该活动");
        }

        ensureCapacityAvailable(activity);

        if (existing != null) {
            existing.setStatus(REG_REGISTERED);
            activityRegistrationMapper.updateById(existing);
        } else {
            ActivityRegistration registration = new ActivityRegistration();
            registration.setActivityId(activity.getId());
            registration.setUserId(user.getId());
            registration.setStatus(REG_REGISTERED);
            activityRegistrationMapper.insert(registration);
        }

        invitation.setStatus(INVITE_ACCEPTED);
        activityInvitationMapper.updateById(invitation);
        return toVO(invitation);
    }

    @Transactional
    public ActivityInvitationVO rejectInvitation(Long invitationId) {
        SysUser user = authSupport.requireStudent();
        ActivityInvitation invitation = getOwnedInvitationOrThrow(invitationId, user.getId());
        if (invitation.getStatus() == null || invitation.getStatus() != INVITE_PENDING) {
            throw new BusinessException(400, "该邀请已处理，无法重复操作");
        }

        invitation.setStatus(INVITE_REJECTED);
        activityInvitationMapper.updateById(invitation);

        ActivityRegistration existing = activityRegistrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, invitation.getActivityId())
                        .eq(ActivityRegistration::getUserId, user.getId()));
        if (existing != null && existing.getStatus() != REG_REGISTERED && existing.getStatus() != REG_CHECKED_IN) {
            existing.setStatus(REG_REJECTED_INVITE);
            activityRegistrationMapper.updateById(existing);
        }

        return toVO(invitation);
    }

    private void ensureCapacityAvailable(Activity activity) {
        if (activity.getMaxParticipants() == null || activity.getMaxParticipants() <= 0) {
            return;
        }
        Long count = activityRegistrationMapper.selectCount(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activity.getId())
                .in(ActivityRegistration::getStatus, REG_REGISTERED, REG_CHECKED_IN));
        if (count != null && count >= activity.getMaxParticipants()) {
            throw new BusinessException(400, "活动报名人数已满");
        }
    }

    private ActivityInvitation getOwnedInvitationOrThrow(Long invitationId, Long userId) {
        ActivityInvitation invitation = activityInvitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BusinessException(404, "活动邀请不存在");
        }
        if (!Objects.equals(invitation.getToUserId(), userId)) {
            throw new BusinessException(403, "无权操作该活动邀请");
        }
        return invitation;
    }

    private ActivityInvitationVO toVO(ActivityInvitation invitation) {
        Activity activity = activityLifecycleService.refreshById(invitation.getActivityId());
        SysOrganization org = activity != null ? orgMapper.selectById(activity.getOrgId()) : null;
        Map<Long, String> nameMap = loadNameMap(invitation);
        return ActivityInvitationVO.builder()
                .id(invitation.getId())
                .activityId(invitation.getActivityId())
                .activityTitle(activity != null ? activity.getTitle() : null)
                .orgId(activity != null ? activity.getOrgId() : null)
                .orgName(org != null ? org.getName() : null)
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

    private List<ActivityInvitationVO> toVOList(List<ActivityInvitation> invitations) {
        if (invitations.isEmpty()) {
            return List.of();
        }
        Map<Long, Activity> activityMap = activityLifecycleService.refreshAll(
                        activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                                .in(Activity::getId, invitations.stream().map(ActivityInvitation::getActivityId).distinct().toList())))
                .stream()
                .collect(Collectors.toMap(Activity::getId, a -> a, (a, b) -> a));
        Map<Long, String> orgNameMap = orgMapper.selectList(new LambdaQueryWrapper<SysOrganization>()
                        .in(SysOrganization::getId, activityMap.values().stream().map(Activity::getOrgId).distinct().toList()))
                .stream()
                .collect(Collectors.toMap(SysOrganization::getId, SysOrganization::getName));
        Map<Long, String> nameMap = loadNameMap(invitations);
        return invitations.stream()
                .map(item -> {
                    Activity activity = activityMap.get(item.getActivityId());
                    String orgName = activity != null ? orgNameMap.get(activity.getOrgId()) : null;
                    return ActivityInvitationVO.builder()
                            .id(item.getId())
                            .activityId(item.getActivityId())
                            .activityTitle(activity != null ? activity.getTitle() : null)
                            .orgId(activity != null ? activity.getOrgId() : null)
                            .orgName(orgName)
                            .location(activity != null ? activity.getLocation() : null)
                            .startTime(activity != null ? activity.getStartTime() : null)
                            .endTime(activity != null ? activity.getEndTime() : null)
                            .creditReward(activity != null ? activity.getCreditReward() : null)
                            .fromUserId(item.getFromUserId())
                            .fromUserName(nameMap.get(item.getFromUserId()))
                            .toUserId(item.getToUserId())
                            .toUserName(nameMap.get(item.getToUserId()))
                            .messageId(item.getMessageId())
                            .status(item.getStatus())
                            .statusName(EnterpriseInterviewService.inviteStatusName(item.getStatus()))
                            .createTime(item.getCreateTime())
                            .build();
                })
                .toList();
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
