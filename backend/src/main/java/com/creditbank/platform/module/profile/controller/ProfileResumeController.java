package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.ResumeAiGenerateRequest;
import com.creditbank.platform.module.profile.dto.ResumeContentVO;
import com.creditbank.platform.module.profile.dto.UpdateUserResumeRequest;
import com.creditbank.platform.module.profile.dto.UserResumeSummaryVO;
import com.creditbank.platform.module.profile.dto.UserResumeVO;
import com.creditbank.platform.module.profile.service.ProfileResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/resumes")
@RequiredArgsConstructor
public class ProfileResumeController {

    private final ProfileResumeService profileResumeService;

    @GetMapping
    public Result<List<UserResumeSummaryVO>> listMyResumes() {
        return Result.ok(profileResumeService.listMyResumes());
    }

    @GetMapping("/default")
    public Result<UserResumeVO> getMyDefaultResume() {
        return Result.ok(profileResumeService.getMyDefaultResume());
    }

    @PostMapping("/ai-generate")
    public Result<ResumeContentVO> generateResumeWithAi(@RequestBody(required = false) ResumeAiGenerateRequest request) {
        return Result.ok(profileResumeService.generateResumeWithAi(request));
    }

    @GetMapping("/{id}")
    public Result<UserResumeVO> getMyResume(@PathVariable Long id) {
        return Result.ok(profileResumeService.getMyResume(id));
    }

    @PostMapping
    public Result<UserResumeVO> createMyResume(@RequestBody UpdateUserResumeRequest request) {
        return Result.ok(profileResumeService.createMyResume(request));
    }

    @PutMapping("/{id}")
    public Result<UserResumeVO> updateMyResume(
            @PathVariable Long id, @RequestBody UpdateUserResumeRequest request) {
        return Result.ok(profileResumeService.updateMyResume(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMyResume(@PathVariable Long id) {
        profileResumeService.deleteMyResume(id);
        return Result.ok();
    }

    @PostMapping("/{id}/default")
    public Result<UserResumeVO> setDefaultResume(@PathVariable Long id) {
        return Result.ok(profileResumeService.setDefaultResume(id));
    }
}
