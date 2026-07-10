package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.JobManageVO;
import com.creditbank.platform.module.enterprise.dto.JobSaveRequest;
import com.creditbank.platform.module.enterprise.service.EnterpriseJobService;
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
@RequestMapping("/api/enterprise/my/jobs")
@RequiredArgsConstructor
public class EnterpriseJobController {

    private final EnterpriseJobService enterpriseJobService;

    @GetMapping
    public Result<List<JobManageVO>> listMyJobs() {
        return Result.ok(enterpriseJobService.listMyJobs());
    }

    @GetMapping("/{id}")
    public Result<JobManageVO> getMyJob(@PathVariable Long id) {
        return Result.ok(enterpriseJobService.getMyJob(id));
    }

    @PostMapping
    public Result<JobManageVO> createJob(@Valid @RequestBody JobSaveRequest request) {
        return Result.ok(enterpriseJobService.createJob(request));
    }

    @PutMapping("/{id}")
    public Result<JobManageVO> updateJob(@PathVariable Long id, @Valid @RequestBody JobSaveRequest request) {
        return Result.ok(enterpriseJobService.updateJob(id, request));
    }

    @PostMapping("/{id}/offline")
    public Result<JobManageVO> offlineJob(@PathVariable Long id) {
        return Result.ok(enterpriseJobService.offlineJob(id));
    }

    @PostMapping("/{id}/online")
    public Result<JobManageVO> onlineJob(@PathVariable Long id) {
        return Result.ok(enterpriseJobService.onlineJob(id));
    }
}
