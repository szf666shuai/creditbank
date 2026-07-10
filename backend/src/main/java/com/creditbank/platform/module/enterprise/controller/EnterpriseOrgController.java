package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.OrgVO;
import com.creditbank.platform.module.enterprise.dto.UpdateMyOrgRequest;
import com.creditbank.platform.module.enterprise.service.EnterpriseOrgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/my/org")
@RequiredArgsConstructor
public class EnterpriseOrgController {

    private final EnterpriseOrgService enterpriseOrgService;

    @GetMapping
    public Result<OrgVO> getMyOrg() {
        return Result.ok(enterpriseOrgService.getMyOrg());
    }

    @PutMapping
    public Result<OrgVO> updateMyOrg(@Valid @RequestBody UpdateMyOrgRequest request) {
        return Result.ok(enterpriseOrgService.updateMyOrg(request));
    }
}
