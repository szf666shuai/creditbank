package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.InterviewInvitationVO;
import com.creditbank.platform.module.enterprise.dto.InterviewRtcCredentialsVO;
import com.creditbank.platform.module.enterprise.dto.SendInterviewInviteRequest;
import com.creditbank.platform.module.enterprise.service.EnterpriseInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/my/interviews")
@RequiredArgsConstructor
public class EnterpriseInterviewController {

    private final EnterpriseInterviewService enterpriseInterviewService;

    @GetMapping
    public Result<List<InterviewInvitationVO>> listSentInvitations() {
        return Result.ok(enterpriseInterviewService.listSentInvitations());
    }

    @PostMapping
    public Result<InterviewInvitationVO> sendInvitation(@Valid @RequestBody SendInterviewInviteRequest request) {
        return Result.ok(enterpriseInterviewService.sendInvitation(request));
    }

    @GetMapping("/{id}/rtc-credentials")
    public Result<InterviewRtcCredentialsVO> getRtcCredentials(@PathVariable Long id) {
        return Result.ok(enterpriseInterviewService.getRtcCredentials(id));
    }
}
