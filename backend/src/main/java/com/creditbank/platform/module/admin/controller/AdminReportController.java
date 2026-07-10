package com.creditbank.platform.module.admin.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.admin.dto.AdminForumReportVO;
import com.creditbank.platform.module.admin.dto.HandleForumReportRequest;
import com.creditbank.platform.module.admin.service.AdminReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public Result<PageResult<AdminForumReportVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(adminReportService.pageReports(page, pageSize, status));
    }

    @PostMapping("/{id}/handle")
    public Result<AdminForumReportVO> handle(
            @PathVariable Long id,
            @Valid @RequestBody HandleForumReportRequest request) {
        return Result.ok(adminReportService.handleReport(id, request));
    }
}
