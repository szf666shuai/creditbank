package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.CreditAccountVO;
import com.creditbank.platform.dto.CreditChangeResult;
import com.creditbank.platform.dto.CreditEarnRequest;
import com.creditbank.platform.dto.CreditRuleVO;
import com.creditbank.platform.dto.CreditSpendRequest;
import com.creditbank.platform.dto.CreditTransactionVO;
import com.creditbank.platform.service.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/account")
    public Result<CreditAccountVO> account() {
        return Result.ok(creditService.getAccount(UserContext.getUserId()));
    }

    @GetMapping("/rules")
    public Result<List<CreditRuleVO>> rules() {
        return Result.ok(creditService.listEnabledRules());
    }

    @GetMapping("/transactions")
    public Result<List<CreditTransactionVO>> transactions(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(creditService.listTransactions(UserContext.getUserId(), limit));
    }

    /** 按规则入账（供业务联调 / 内部调用） */
    @PostMapping("/earn")
    public Result<CreditChangeResult> earn(@Valid @RequestBody CreditEarnRequest request) {
        return Result.ok(creditService.earnByRule(UserContext.getUserId(), request));
    }

    /** 消耗学分（商城/报名等） */
    @PostMapping("/spend")
    public Result<CreditChangeResult> spend(@Valid @RequestBody CreditSpendRequest request) {
        return Result.ok(creditService.spend(UserContext.getUserId(), request));
    }
}
