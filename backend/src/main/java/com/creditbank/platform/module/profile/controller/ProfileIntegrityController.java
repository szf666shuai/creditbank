package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.IntegrityRecordVO;
import com.creditbank.platform.module.profile.dto.IntegritySummaryVO;
import com.creditbank.platform.module.profile.service.ProfileIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/profile/integrity")
@RequiredArgsConstructor
public class ProfileIntegrityController {

    private final ProfileIntegrityService profileIntegrityService;

    @GetMapping("/summary")
    public Result<IntegritySummaryVO> getSummary() {
        return Result.ok(profileIntegrityService.getSummary());
    }

    @GetMapping("/records")
    public Result<PageResult<IntegrityRecordVO>> pageRecords(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(profileIntegrityService.pageRecords(page, pageSize, eventType, startDate, endDate));
    }
}
