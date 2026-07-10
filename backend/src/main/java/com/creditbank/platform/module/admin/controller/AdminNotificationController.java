package com.creditbank.platform.module.admin.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.admin.dto.SendSystemNotificationRequest;
import com.creditbank.platform.module.admin.service.AdminNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping
    public Result<Map<String, Integer>> send(@Valid @RequestBody SendSystemNotificationRequest request) {
        int sentCount = adminNotificationService.sendSystemNotification(request);
        return Result.ok(Map.of("sentCount", sentCount));
    }
}
