package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.ApplicationManageVO;
import com.creditbank.platform.module.enterprise.service.EnterpriseApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/my/applications")
@RequiredArgsConstructor
public class EnterpriseApplicationController {

    private final EnterpriseApplicationService enterpriseApplicationService;

    @GetMapping
    public Result<List<ApplicationManageVO>> listApplications() {
        return Result.ok(enterpriseApplicationService.listApplications());
    }
}
