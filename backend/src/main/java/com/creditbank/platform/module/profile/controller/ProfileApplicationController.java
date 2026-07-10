package com.creditbank.platform.module.profile.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.profile.dto.MyJobApplicationVO;
import com.creditbank.platform.module.profile.service.ProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile/applications")
@RequiredArgsConstructor
public class ProfileApplicationController {

    private final ProfileApplicationService profileApplicationService;

    @GetMapping
    public Result<List<MyJobApplicationVO>> listMyApplications(
            @RequestParam(required = false) Integer status) {
        return Result.ok(profileApplicationService.listMyApplications(status));
    }
}
