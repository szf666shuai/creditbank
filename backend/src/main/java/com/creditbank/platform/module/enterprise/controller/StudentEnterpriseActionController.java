package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.ActivityRegisterResultVO;
import com.creditbank.platform.module.enterprise.dto.ApplyJobRequest;
import com.creditbank.platform.module.enterprise.dto.JobApplyResultVO;
import com.creditbank.platform.module.enterprise.dto.OrgParticipationStatusVO;
import com.creditbank.platform.module.enterprise.service.StudentEnterpriseActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class StudentEnterpriseActionController {

    private final StudentEnterpriseActionService studentEnterpriseActionService;

    @GetMapping("/my/participation")
    public Result<OrgParticipationStatusVO> getParticipationStatus(@RequestParam Long orgId) {
        return Result.ok(studentEnterpriseActionService.getOrgParticipationStatus(orgId));
    }

    @PostMapping("/jobs/{jobId}/apply")
    public Result<JobApplyResultVO> applyJob(
            @PathVariable Long jobId,
            @RequestBody(required = false) ApplyJobRequest request) {
        return Result.ok(studentEnterpriseActionService.applyJob(jobId, request));
    }

    @PostMapping("/activities/{activityId}/register")
    public Result<ActivityRegisterResultVO> registerActivity(@PathVariable Long activityId) {
        return Result.ok(studentEnterpriseActionService.registerActivity(activityId));
    }
}
