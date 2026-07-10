package com.creditbank.platform.module.admin.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.admin.dto.AdminOrganizationVO;
import com.creditbank.platform.module.admin.dto.UpdateOrganizationJoinStatusRequest;
import com.creditbank.platform.module.admin.dto.UpdateOrganizationStatusRequest;
import com.creditbank.platform.module.admin.service.AdminOrganizationService;
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
@RequestMapping("/api/admin/organizations")
@RequiredArgsConstructor
public class AdminOrganizationController {

    private final AdminOrganizationService adminOrganizationService;

    @GetMapping
    public Result<PageResult<AdminOrganizationVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer joinStatus,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminOrganizationService.pageOrganizations(page, pageSize, joinStatus, status, keyword));
    }

    @PatchMapping("/{id}/join-status")
    public Result<AdminOrganizationVO> updateJoinStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationJoinStatusRequest request) {
        return Result.ok(adminOrganizationService.updateJoinStatus(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<AdminOrganizationVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return Result.ok(adminOrganizationService.updateStatus(id, request));
    }
}
