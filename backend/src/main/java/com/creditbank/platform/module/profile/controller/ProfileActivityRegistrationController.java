package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.MyActivityRegistrationVO;
import com.creditbank.platform.module.profile.service.ProfileActivityRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/activity-registrations")
@RequiredArgsConstructor
public class ProfileActivityRegistrationController {

    private final ProfileActivityRegistrationService profileActivityRegistrationService;

    @GetMapping
    public Result<List<MyActivityRegistrationVO>> listMyRegistrations() {
        return Result.ok(profileActivityRegistrationService.listMyRegistrations());
    }
}
