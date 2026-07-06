package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "application", "credit-bank-platform",
                "version", "1.0.0-SNAPSHOT"
        ));
    }
}
