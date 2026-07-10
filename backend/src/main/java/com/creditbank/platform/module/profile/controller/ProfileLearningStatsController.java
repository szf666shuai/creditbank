package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.LearningStatDailyVO;
import com.creditbank.platform.module.profile.service.ProfileLearningStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/profile/learning-stats")
@RequiredArgsConstructor
public class ProfileLearningStatsController {

    private final ProfileLearningStatsService profileLearningStatsService;

    @GetMapping
    public Result<List<LearningStatDailyVO>> listDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(profileLearningStatsService.listDailyStats(startDate, endDate));
    }
}
