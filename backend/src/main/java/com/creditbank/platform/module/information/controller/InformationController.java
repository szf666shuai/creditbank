package com.creditbank.platform.module.information.controller;

import com.creditbank.platform.common.PageResult;
import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.information.dto.InformationDetailVO;
import com.creditbank.platform.module.information.dto.InformationItemVO;
import com.creditbank.platform.module.information.service.InformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/information")
@RequiredArgsConstructor
public class InformationController {

    private final InformationService informationService;

    @GetMapping("/{type}")
    public Result<PageResult<InformationItemVO>> page(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(informationService.page(type, page, pageSize, keyword));
    }

    @GetMapping("/{type}/{id}")
    public Result<InformationDetailVO> detail(@PathVariable String type, @PathVariable Long id) {
        return Result.ok(informationService.getDetail(type, id));
    }
}
