package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.ActivityManageVO;
import com.creditbank.platform.module.enterprise.dto.ActivitySaveRequest;
import com.creditbank.platform.module.enterprise.service.EnterpriseActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/my/activities")
@RequiredArgsConstructor
public class EnterpriseActivityController {

    private final EnterpriseActivityService enterpriseActivityService;

    @GetMapping
    public Result<List<ActivityManageVO>> listMyActivities() {
        return Result.ok(enterpriseActivityService.listMyActivities());
    }

    @GetMapping("/{id}")
    public Result<ActivityManageVO> getMyActivity(@PathVariable Long id) {
        return Result.ok(enterpriseActivityService.getMyActivity(id));
    }

    @PostMapping
    public Result<ActivityManageVO> createActivity(@Valid @RequestBody ActivitySaveRequest request) {
        return Result.ok(enterpriseActivityService.createActivity(request));
    }

    @PutMapping("/{id}")
    public Result<ActivityManageVO> updateActivity(@PathVariable Long id,
                                                   @Valid @RequestBody ActivitySaveRequest request) {
        return Result.ok(enterpriseActivityService.updateActivity(id, request));
    }

    @PostMapping("/{id}/cancel")
    public Result<ActivityManageVO> cancelActivity(@PathVariable Long id) {
        return Result.ok(enterpriseActivityService.cancelActivity(id));
    }
}
