package com.creditbank.platform.module.admin.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.admin.dto.AdminUserVO;
import com.creditbank.platform.module.admin.dto.CreateEnterpriseUserRequest;
import com.creditbank.platform.module.admin.dto.UpdateUserStatusRequest;
import com.creditbank.platform.module.admin.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<AdminUserVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminUserService.pageUsers(page, pageSize, role, status, keyword));
    }

    @PatchMapping("/{id}/status")
    public Result<AdminUserVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return Result.ok(adminUserService.updateStatus(id, request));
    }

    @PostMapping("/enterprise")
    public Result<AdminUserVO> createEnterpriseUser(@Valid @RequestBody CreateEnterpriseUserRequest request) {
        return Result.ok(adminUserService.createEnterpriseUser(request));
    }
}
