package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.IntegrityAdjustRequest;
import com.creditbank.platform.dto.IntegrityRecordVO;
import com.creditbank.platform.dto.IntegrityScoreVO;
import com.creditbank.platform.service.IntegrityService;
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
@RequestMapping("/api/integrity")
@RequiredArgsConstructor
public class IntegrityController {

    private final IntegrityService integrityService;

    @GetMapping("/me")
    public Result<IntegrityScoreVO> me() {
        return Result.ok(integrityService.getMyScore(UserContext.getUserId()));
    }

    @GetMapping("/records")
    public Result<List<IntegrityRecordVO>> records(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(integrityService.listRecords(UserContext.getUserId(), limit));
    }

    /**
     * 诚信分调整（当前开放给登录用户自测/联调；后续应收口到管理员角色）。
     */
    @PostMapping("/adjust")
    public Result<IntegrityScoreVO> adjust(@Valid @RequestBody IntegrityAdjustRequest request) {
        Long userId = UserContext.getUserId();
        return Result.ok(integrityService.adjust(userId, request, userId));
    }
}
