package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.ProfileSummaryVO;
import com.creditbank.platform.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/summary")
    public Result<ProfileSummaryVO> summary() {
        return Result.ok(profileService.getSummary(UserContext.getUserId()));
    }
}
