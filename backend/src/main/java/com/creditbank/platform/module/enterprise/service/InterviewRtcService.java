package com.creditbank.platform.module.enterprise.service;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.config.TrtcProperties;
import com.creditbank.platform.integration.trtc.TlsSigApiV2;
import com.creditbank.platform.module.enterprise.dto.InterviewRtcCredentialsVO;
import com.creditbank.platform.module.enterprise.entity.InterviewInvitation;
import com.creditbank.platform.module.enterprise.entity.JobApplication;
import com.creditbank.platform.module.enterprise.mapper.InterviewInvitationMapper;
import com.creditbank.platform.module.enterprise.mapper.JobApplicationMapper;
import com.creditbank.platform.module.enterprise.support.ApplicationStatus;
import com.creditbank.platform.module.enterprise.support.InterviewMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InterviewRtcService {

    private static final int INVITE_ACCEPTED = 1;

    private final TrtcProperties trtcProperties;
    private final InterviewInvitationMapper interviewInvitationMapper;
    private final JobApplicationMapper jobApplicationMapper;

    public InterviewRtcCredentialsVO issueCredentials(InterviewInvitation invitation, Long userId) {
        requireConfigured();
        validateParticipant(invitation, userId);
        validateJoinable(invitation);

        String roomId = resolveRoomId(invitation);
        String rtcUserId = toRtcUserId(userId);
        TlsSigApiV2 sigApi = new TlsSigApiV2(trtcProperties.getSdkAppId(), trtcProperties.getSecretKey());
        String userSig = sigApi.genUserSig(rtcUserId, trtcProperties.getExpireSeconds());
        if (!StringUtils.hasText(userSig)) {
            throw new BusinessException(500, "生成视频鉴权票据失败");
        }

        return InterviewRtcCredentialsVO.builder()
                .invitationId(invitation.getId())
                .sdkAppId(trtcProperties.getSdkAppId())
                .userId(rtcUserId)
                .userSig(userSig)
                .roomId(roomId)
                .build();
    }

    public InterviewInvitation requireInvitation(Long invitationId) {
        InterviewInvitation invitation = interviewInvitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BusinessException(404, "面试邀请不存在");
        }
        return invitation;
    }

    public boolean canJoinVideo(InterviewInvitation invitation) {
        if (!trtcProperties.isConfigured()) {
            return false;
        }
        try {
            validateJoinable(invitation);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void requireConfigured() {
        if (!trtcProperties.isConfigured()) {
            throw new BusinessException(503, "视频面试服务未配置，请联系管理员");
        }
    }

    private void validateParticipant(InterviewInvitation invitation, Long userId) {
        if (!Objects.equals(invitation.getFromUserId(), userId)
                && !Objects.equals(invitation.getToUserId(), userId)) {
            throw new BusinessException(403, "无权进入该面试房间");
        }
    }

    private void validateJoinable(InterviewInvitation invitation) {
        if (!Objects.equals(invitation.getStatus(), INVITE_ACCEPTED)) {
            throw new BusinessException(400, "学员接受邀请后方可进入面试");
        }
        if (!Objects.equals(invitation.getInterviewMode(), InterviewMode.VIDEO)) {
            throw new BusinessException(400, "该面试不是平台视频面试");
        }
        if (invitation.getInviteTime() == null) {
            throw new BusinessException(400, "面试时间未设置");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = invitation.getInviteTime()
                .minusMinutes(trtcProperties.getJoinWindowMinutesBefore());
        LocalDateTime windowEnd = invitation.getInviteTime()
                .plusMinutes(trtcProperties.getJoinWindowMinutesAfter());
        if (now.isBefore(windowStart)) {
            throw new BusinessException(400, "面试尚未开始，请在约定时间前 "
                    + trtcProperties.getJoinWindowMinutesBefore() + " 分钟内进入");
        }
        if (now.isAfter(windowEnd)) {
            throw new BusinessException(400, "面试房间已关闭");
        }
        validateApplicationInterviewing(invitation);
    }

    private void validateApplicationInterviewing(InterviewInvitation invitation) {
        if (invitation.getApplicationId() == null) {
            return;
        }
        JobApplication application = jobApplicationMapper.selectById(invitation.getApplicationId());
        if (application == null || !ApplicationStatus.canEnterInterview(application.getStatus())) {
            throw new BusinessException(400, "该投递已结束面试流程，无法进入面试");
        }
    }

    private String resolveRoomId(InterviewInvitation invitation) {
        if (StringUtils.hasText(invitation.getRoomId())) {
            return invitation.getRoomId();
        }
        String roomId = "interview-" + invitation.getId();
        invitation.setRoomId(roomId);
        interviewInvitationMapper.updateById(invitation);
        return roomId;
    }

    public static String toRtcUserId(Long userId) {
        return "u" + userId;
    }
}
