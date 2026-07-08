package com.creditbank.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {
    private List<SearchItemVO> courses;
    private List<SearchItemVO> hotProducts;
    private List<SearchItemVO> hotActivities;
    private List<SearchItemVO> microMajors;
    private List<SearchItemVO> hotNews;
    private List<SearchItemVO> jobs;
    private List<SearchItemVO> forumPosts;
    private List<SearchItemVO> partners;
}
