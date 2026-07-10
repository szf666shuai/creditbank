package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.ProfileDashboardVO;
import com.creditbank.platform.module.profile.service.ProfileDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileDashboardController {

    private final ProfileDashboardService profileDashboardService;

    @GetMapping("/dashboard")
    public Result<ProfileDashboardVO> getDashboard() {
        return Result.ok(profileDashboardService.getDashboard());
    }
}
