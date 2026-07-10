package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.ActivityInvitationVO;
import com.creditbank.platform.module.enterprise.dto.SendActivityInviteRequest;
import com.creditbank.platform.module.enterprise.service.EnterpriseActivityInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/my/activity-invitations")
@RequiredArgsConstructor
public class EnterpriseActivityInvitationController {

    private final EnterpriseActivityInvitationService enterpriseActivityInvitationService;

    @GetMapping
    public Result<List<ActivityInvitationVO>> listSentInvitations() {
        return Result.ok(enterpriseActivityInvitationService.listSentInvitations());
    }

    @PostMapping
    public Result<ActivityInvitationVO> sendInvitation(@Valid @RequestBody SendActivityInviteRequest request) {
        return Result.ok(enterpriseActivityInvitationService.sendInvitation(request));
    }
}
