package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.CertificateVerifyResult;
import com.creditbank.platform.dto.LearningArchiveVO;
import com.creditbank.platform.dto.LearningCertificateVO;
import com.creditbank.platform.dto.LearningCompletionResult;
import com.creditbank.platform.dto.LearningProgressRequest;
import com.creditbank.platform.dto.LearningResourceVO;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/resources")
    public Result<List<LearningResourceVO>> resources(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String tag) {
        return Result.ok(learningService.listResources(UserContext.getUserId(), keyword, tag));
    }

    @GetMapping("/tags")
    public Result<List<String>> tags() {
        return Result.ok(learningService.listSkillTags());
    }

    @PostMapping("/resources/{courseId}/start")
    public Result<UserCourse> start(@PathVariable Long courseId) {
        return Result.ok(learningService.startCourse(UserContext.getUserId(), courseId));
    }

    @PostMapping("/resources/{courseId}/progress")
    public Result<UserCourse> progress(@PathVariable Long courseId,
                                       @Valid @RequestBody LearningProgressRequest request) {
        return Result.ok(learningService.reportProgress(UserContext.getUserId(), courseId, request));
    }

    @PostMapping("/resources/{courseId}/complete")
    public Result<LearningCompletionResult> complete(@PathVariable Long courseId) {
        return Result.ok(learningService.completeCourse(UserContext.getUserId(), courseId));
    }

    @GetMapping("/archives")
    public Result<List<LearningArchiveVO>> archives(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(learningService.listArchives(UserContext.getUserId(), limit));
    }

    @GetMapping("/certificates")
    public Result<List<LearningCertificateVO>> certificates(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(learningService.listCertificates(UserContext.getUserId(), limit));
    }

    @GetMapping("/certificates/verify")
    public Result<CertificateVerifyResult> verify(@RequestParam String certNo,
                                                  @RequestParam(required = false) String hash) {
        return Result.ok(learningService.verifyCertificate(certNo, hash));
    }
}
