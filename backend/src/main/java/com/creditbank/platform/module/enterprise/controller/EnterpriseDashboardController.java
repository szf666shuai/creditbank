package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.EnterpriseDashboardVO;
import com.creditbank.platform.module.enterprise.service.EnterpriseDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/my/dashboard")
@RequiredArgsConstructor
public class EnterpriseDashboardController {

    private final EnterpriseDashboardService enterpriseDashboardService;

    @GetMapping
    public Result<EnterpriseDashboardVO> getDashboard() {
        return Result.ok(enterpriseDashboardService.getDashboard());
    }
}
