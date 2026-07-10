package com.creditbank.platform.module.admin.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.admin.dto.AdminActivityVO;
import com.creditbank.platform.module.admin.dto.AdminCreditTransactionVO;
import com.creditbank.platform.module.admin.dto.AdminIntegrityRecordVO;
import com.creditbank.platform.module.admin.dto.AdminJobVO;
import com.creditbank.platform.module.admin.dto.UpdateContentStatusRequest;
import com.creditbank.platform.module.admin.service.AdminOversightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/oversight")
@RequiredArgsConstructor
public class AdminOversightController {

    private final AdminOversightService adminOversightService;

    @GetMapping("/jobs")
    public Result<PageResult<AdminJobVO>> pageJobs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminOversightService.pageJobs(page, pageSize, status, keyword));
    }

    @PatchMapping("/jobs/{id}/status")
    public Result<AdminJobVO> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentStatusRequest request) {
        return Result.ok(adminOversightService.updateJobStatus(id, request));
    }

    @GetMapping("/activities")
    public Result<PageResult<AdminActivityVO>> pageActivities(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminOversightService.pageActivities(page, pageSize, status, keyword));
    }

    @PatchMapping("/activities/{id}/status")
    public Result<AdminActivityVO> updateActivityStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentStatusRequest request) {
        return Result.ok(adminOversightService.updateActivityStatus(id, request));
    }

    @GetMapping("/integrity-records")
    public Result<PageResult<AdminIntegrityRecordVO>> pageIntegrityRecords(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer eventType,
            @RequestParam(required = false) Long userId) {
        return Result.ok(adminOversightService.pageIntegrityRecords(page, pageSize, eventType, userId));
    }

    @GetMapping("/credit-transactions")
    public Result<PageResult<AdminCreditTransactionVO>> pageCreditTransactions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long userId) {
        return Result.ok(adminOversightService.pageCreditTransactions(page, pageSize, type, userId));
    }
}
