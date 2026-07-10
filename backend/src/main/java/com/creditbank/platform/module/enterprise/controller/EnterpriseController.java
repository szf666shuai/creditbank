package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.ActivityVO;
import com.creditbank.platform.module.enterprise.dto.JobPostingVO;
import com.creditbank.platform.module.enterprise.dto.OrgMaterialVO;
import com.creditbank.platform.module.enterprise.dto.OrgVO;
import com.creditbank.platform.module.enterprise.dto.TagVO;
import com.creditbank.platform.module.enterprise.service.EnterpriseJobService;
import com.creditbank.platform.module.enterprise.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;
    private final EnterpriseJobService enterpriseJobService;

    @GetMapping("/tags")
    public Result<List<TagVO>> listSkillTags() {
        return Result.ok(enterpriseJobService.listSkillTags());
    }

    @GetMapping("/orgs")
    public Result<PageResult<OrgVO>> listJoinedOrgs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type) {
        return Result.ok(enterpriseService.pageJoinedOrgs(page, pageSize, name, type));
    }

    @GetMapping("/orgs/{id}")
    public Result<OrgVO> getJoinedOrg(@PathVariable Long id) {
        return Result.ok(enterpriseService.getJoinedOrgDetail(id));
    }

    @GetMapping("/orgs/{id}/jobs")
    public Result<PageResult<JobPostingVO>> listOrgJobs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(enterpriseService.pageOrgJobs(id, page, pageSize));
    }

    @GetMapping("/orgs/{id}/activities")
    public Result<PageResult<ActivityVO>> listOrgActivities(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(enterpriseService.pageOrgActivities(id, page, pageSize));
    }

    @GetMapping("/orgs/{id}/materials")
    public Result<PageResult<OrgMaterialVO>> listOrgMaterials(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(enterpriseService.pageOrgMaterials(id, page, pageSize));
    }
}
