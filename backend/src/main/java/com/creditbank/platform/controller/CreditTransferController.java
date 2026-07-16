package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.dto.CreditTransferAiScreenResult;
import com.creditbank.platform.dto.CreditTransferApplyRequest;
import com.creditbank.platform.dto.CreditTransferApplicationVO;
import com.creditbank.platform.dto.CreditTransferRuleVO;
import com.creditbank.platform.entity.CreditTransferRule;
import com.creditbank.platform.entity.SysUser;
import com.creditbank.platform.security.AuthSupport;
import com.creditbank.platform.service.CreditTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/credit-transfer")
@RequiredArgsConstructor
public class CreditTransferController {

    private final CreditTransferService creditTransferService;
    private final AuthSupport authSupport;

    @GetMapping("/rules")
    public Result<List<CreditTransferRuleVO>> listRules() {
        SysUser user = authSupport.requireEnterprise();
        return Result.ok(creditTransferService.listRules(user.getOrgId()));
    }

    /** 公开：学员/访客查看某机构已启用的学分转换规则 */
    @GetMapping("/org/{orgId}/rules")
    public Result<List<CreditTransferRuleVO>> listOrgPublicRules(@PathVariable Long orgId) {
        return Result.ok(creditTransferService.listEnabledRules(orgId));
    }

    @GetMapping("/rules/{ruleId}")
    public Result<CreditTransferRuleVO> getRule(@PathVariable Long ruleId) {
        return Result.ok(creditTransferService.getRule(ruleId));
    }

    @PostMapping("/rules")
    public Result<CreditTransferRuleVO> createRule(@RequestBody CreditTransferRule rule) {
        return Result.ok(creditTransferService.createRule(rule));
    }

    @PutMapping("/rules/{ruleId}")
    public Result<CreditTransferRuleVO> updateRule(
            @PathVariable Long ruleId,
            @RequestBody CreditTransferRule rule) {
        return Result.ok(creditTransferService.updateRule(ruleId, rule));
    }

    @DeleteMapping("/rules/{ruleId}")
    public Result<Void> deleteRule(@PathVariable Long ruleId) {
        creditTransferService.deleteRule(ruleId);
        return Result.ok();
    }

    @PostMapping("/apply")
    public Result<CreditTransferApplicationVO> apply(@Valid @RequestBody CreditTransferApplyRequest request) {
        return Result.ok(creditTransferService.apply(request));
    }

    @GetMapping("/applications")
    public Result<List<CreditTransferApplicationVO>> listApplications(
            @RequestParam(required = false) Integer status) {
        SysUser user = authSupport.requireEnterprise();
        return Result.ok(creditTransferService.listApplications(user.getOrgId(), status));
    }

    @GetMapping("/my-applications")
    public Result<List<CreditTransferApplicationVO>> listMyApplications() {
        SysUser user = authSupport.requireStudent();
        return Result.ok(creditTransferService.listMyApplications(user.getId()));
    }

    @PutMapping("/applications/{applicationId}/review")
    public Result<CreditTransferApplicationVO> review(
            @PathVariable Long applicationId,
            @RequestParam Integer status,
            @RequestParam(required = false) String comment) {
        return Result.ok(creditTransferService.review(applicationId, status, comment));
    }

    @PostMapping("/applications/{applicationId}/ai-screen")
    public Result<CreditTransferAiScreenResult> aiScreen(
            @PathVariable Long applicationId) {
        return Result.ok(creditTransferService.aiScreen(applicationId));
    }

    @GetMapping("/match-rules")
    public Result<List<CreditTransferRuleVO>> matchRules(
            @RequestParam Integer sourceType,
            @RequestParam(required = false) Long sourceCourseId,
            @RequestParam(required = false) Long sourceAchievementId) {
        authSupport.requireStudentOrAdmin();
        return Result.ok(creditTransferService.matchRules(sourceType, sourceCourseId, sourceAchievementId));
    }
}