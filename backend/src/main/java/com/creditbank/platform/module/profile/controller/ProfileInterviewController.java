package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.InterviewInvitationVO;
import com.creditbank.platform.module.enterprise.dto.InterviewRtcCredentialsVO;
import com.creditbank.platform.module.profile.service.ProfileInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/interviews")
@RequiredArgsConstructor
public class ProfileInterviewController {

    private final ProfileInterviewService profileInterviewService;

    @GetMapping
    public Result<List<InterviewInvitationVO>> listMyInvitations() {
        return Result.ok(profileInterviewService.listMyInvitations());
    }

    @GetMapping("/{id}")
    public Result<InterviewInvitationVO> getMyInvitation(@PathVariable Long id) {
        return Result.ok(profileInterviewService.getMyInvitation(id));
    }

    @PostMapping("/{id}/accept")
    public Result<InterviewInvitationVO> acceptInvitation(@PathVariable Long id) {
        return Result.ok(profileInterviewService.acceptInvitation(id));
    }

    @PostMapping("/{id}/reject")
    public Result<InterviewInvitationVO> rejectInvitation(@PathVariable Long id) {
        return Result.ok(profileInterviewService.rejectInvitation(id));
    }

    @GetMapping("/{id}/rtc-credentials")
    public Result<InterviewRtcCredentialsVO> getRtcCredentials(@PathVariable Long id) {
        return Result.ok(profileInterviewService.getRtcCredentials(id));
    }
}
