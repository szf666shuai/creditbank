package com.creditbank.platform.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.constant.UserRole;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.entity.SysOrganization;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.mapper.SysOrganizationMapper;
import com.creditbank.platform.mapper.SysUserMapper;
import com.creditbank.platform.module.enterprise.entity.Activity;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.entity.JobPosting;
import com.creditbank.platform.module.enterprise.mapper.ActivityMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobPostingMapper;
import com.creditbank.platform.module.message.entity.UserMessage;
import com.creditbank.platform.module.message.mapper.UserMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 统一鉴权：登录态、角色、org 数据隔离、用户私有数据归属。
 */
@Component
@RequiredArgsConstructor
public class AuthSupport {

    private final SysUserMapper userMapper;
    private final SysOrganizationMapper orgMapper;
    private final JobPostingMapper jobPostingMapper;
    private final ActivityMapper activityMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final UserMessageMapper userMessageMapper;

    public SysUser requireLoginUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或登录已过期");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        return user;
    }

    public Long requireUserId() {
        return requireLoginUser().getId();
    }

    public SysUser requireStudent() {
        SysUser user = requireLoginUser();
        if (user.getRole() == null || user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(403, "仅学员用户可操作");
        }
        return user;
    }

    public SysUser requireEnterprise() {
        SysUser user = requireLoginUser();
        if (user.getRole() == null || user.getRole() != UserRole.ENTERPRISE || user.getOrgId() == null) {
            throw new BusinessException(403, "仅企业用户可操作");
        }
        return user;
    }

    /** 企业已加盟且未停用时方可发布职位、活动、邀请等写操作 */
    public SysUser requireEnterpriseWritable() {
        SysUser user = requireEnterprise();
        requireWritableOrganization(user.getOrgId());
        return user;
    }

    public void requireWritableOrganization(Long orgId) {
        if (orgId == null) {
            throw new BusinessException(403, "当前账号未绑定企业机构");
        }
        SysOrganization org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, "企业机构不存在");
        }
        if (org.getJoinStatus() == null || org.getJoinStatus() != 1) {
            throw new BusinessException(403, "机构尚未通过加盟审核，暂不可执行该操作");
        }
        if (org.getStatus() != null && org.getStatus() == 0) {
            throw new BusinessException(403, "机构已停用，暂不可执行该操作");
        }
    }

    public void requireOrganizationNotDisabled(Long orgId) {
        if (orgId == null) {
            throw new BusinessException(403, "当前账号未绑定企业机构");
        }
        SysOrganization org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, "企业机构不存在");
        }
        if (org.getStatus() != null && org.getStatus() == 0) {
            throw new BusinessException(403, "机构已停用，暂不可执行该操作");
        }
    }

    public SysUser requireStudentOrAdmin() {
        SysUser user = requireLoginUser();
        if (user.getRole() != null
                && (user.getRole() == UserRole.STUDENT || user.getRole() == UserRole.ADMIN)) {
            return user;
        }
        throw new BusinessException(403, "仅学员或管理员可浏览学习资源");
    }

    public SysUser requireStudentOrAdminOrEnterprise() {
        SysUser user = requireLoginUser();
        if (user.getRole() != null
                && (user.getRole() == UserRole.STUDENT
                        || user.getRole() == UserRole.ADMIN
                        || user.getRole() == UserRole.ENTERPRISE)) {
            return user;
        }
        throw new BusinessException(403, "仅学员、管理员或企业用户可浏览学习资源");
    }

    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        SysUser user = userMapper.selectById(userId);
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public SysUser requireAdmin() {
        SysUser user = requireLoginUser();
        if (user.getRole() == null || user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(403, "仅系统管理员可操作");
        }
        return user;
    }

    public void requireOwner(Long ownerUserId, Long currentUserId, String resourceLabel) {
        if (!Objects.equals(ownerUserId, currentUserId)) {
            throw new BusinessException(403, "无权操作该" + resourceLabel);
        }
    }

    public JobPosting requireOrgJob(Long jobId, Long orgId) {
        JobPosting job = jobPostingMapper.selectOne(new LambdaQueryWrapper<JobPosting>()
                .eq(JobPosting::getId, jobId)
                .eq(JobPosting::getOrgId, orgId));
        if (job == null) {
            throw new BusinessException(404, "职位不存在或无权操作");
        }
        return job;
    }

    public Activity requireOrgActivity(Long activityId, Long orgId) {
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, activityId)
                .eq(Activity::getOrgId, orgId));
        if (activity == null) {
            throw new BusinessException(404, "活动不存在或无权操作");
        }
        return activity;
    }

    public JobApplication requireOrgApplication(Long applicationId, Long orgId) {
        JobApplication application = jobApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(404, "投递记录不存在");
        }
        requireOrgJob(application.getJobId(), orgId);
        return application;
    }

    public UserMessage requireAccessibleMessage(Long messageId, Long userId) {
        UserMessage message = userMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }
        if (!Objects.equals(message.getFromUserId(), userId) && !Objects.equals(message.getToUserId(), userId)) {
            throw new BusinessException(403, "无权查看该消息");
        }
        return message;
    }

    public UserMessage requireMessageReceiver(Long messageId, Long userId) {
        UserMessage message = userMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }
        if (!Objects.equals(message.getToUserId(), userId)) {
            throw new BusinessException(403, "仅接收方可操作该消息");
        }
        return message;
    }
}
