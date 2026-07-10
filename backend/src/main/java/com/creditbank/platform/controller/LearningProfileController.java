package com.creditbank.platform.controller;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.LearningProfileVO;
import com.creditbank.platform.dto.LearningSituationVO;
import com.creditbank.platform.service.LearningProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/profile")
@RequiredArgsConstructor
public class LearningProfileController {

    private final LearningProfileService learningProfileService;

    @GetMapping("/situation")
    public Result<LearningSituationVO> situation() {
        return Result.ok(learningProfileService.loadSituation(requireUserId()));
    }

    @GetMapping
    public Result<LearningProfileVO> get() {
        return Result.ok(learningProfileService.getProfile(requireUserId()));
    }

    @PostMapping("/generate")
    public Result<LearningProfileVO> generate() {
        return Result.ok(learningProfileService.generate(requireUserId()));
    }

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
