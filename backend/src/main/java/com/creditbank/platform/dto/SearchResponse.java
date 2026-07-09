package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private String keyword;
    private String type;
    private int total;
    private List<SearchItemVO> items;
}
