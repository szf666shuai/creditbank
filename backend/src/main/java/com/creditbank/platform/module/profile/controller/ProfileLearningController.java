package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.LearningAchievementVO;
import com.creditbank.platform.module.profile.dto.LearningArchiveVO;
import com.creditbank.platform.module.profile.service.ProfileLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/learning")
@RequiredArgsConstructor
public class ProfileLearningController {

    private final ProfileLearningService profileLearningService;

    @GetMapping("/archives")
    public Result<List<LearningArchiveVO>> listArchives() {
        return Result.ok(profileLearningService.listMyArchives());
    }

    @GetMapping("/achievements")
    public Result<List<LearningAchievementVO>> listAchievements() {
        return Result.ok(profileLearningService.listMyAchievements());
    }
}
