package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.CreditAccountSummaryVO;
import com.creditbank.platform.module.profile.dto.CreditTransactionVO;
import com.creditbank.platform.module.profile.service.ProfileCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/profile/credit")
@RequiredArgsConstructor
public class ProfileCreditController {

    private final ProfileCreditService profileCreditService;

    @GetMapping("/summary")
    public Result<CreditAccountSummaryVO> getAccountSummary() {
        return Result.ok(profileCreditService.getAccountSummary());
    }

    @GetMapping("/transactions")
    public Result<PageResult<CreditTransactionVO>> pageTransactions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(profileCreditService.pageTransactions(page, pageSize, type, startDate, endDate));
    }
}
