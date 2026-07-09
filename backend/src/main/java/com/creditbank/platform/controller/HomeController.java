package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.dto.HomeResponse;
import com.creditbank.platform.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public Result<HomeResponse> home() {
        return Result.ok(homeService.getHomeData());
    }
}
