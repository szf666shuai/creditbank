package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.dto.SearchResponse;
import com.creditbank.platform.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Result<SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(searchService.search(q, type, limit));
    }
}
