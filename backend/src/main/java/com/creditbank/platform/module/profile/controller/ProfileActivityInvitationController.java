package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.ActivityInvitationVO;
import com.creditbank.platform.module.profile.service.ProfileActivityInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/activity-invitations")
@RequiredArgsConstructor
public class ProfileActivityInvitationController {

    private final ProfileActivityInvitationService profileActivityInvitationService;

    @GetMapping
    public Result<List<ActivityInvitationVO>> listMyInvitations() {
        return Result.ok(profileActivityInvitationService.listMyInvitations());
    }

    @GetMapping("/{id}")
    public Result<ActivityInvitationVO> getMyInvitation(@PathVariable Long id) {
        return Result.ok(profileActivityInvitationService.getMyInvitation(id));
    }

    @PostMapping("/{id}/accept")
    public Result<ActivityInvitationVO> acceptInvitation(@PathVariable Long id) {
        return Result.ok(profileActivityInvitationService.acceptInvitation(id));
    }

    @PostMapping("/{id}/reject")
    public Result<ActivityInvitationVO> rejectInvitation(@PathVariable Long id) {
        return Result.ok(profileActivityInvitationService.rejectInvitation(id));
    }
}
