package com.creditbank.platform.service;

import com.creditbank.platform.dto.HomeResponse;
import com.creditbank.platform.dto.SearchItemVO;
import com.creditbank.platform.mapper.HomeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int COURSE_LIMIT = 8;
    private static final int PRODUCT_LIMIT = 8;
    private static final int ACTIVITY_LIMIT = 6;
    private static final int MICRO_LIMIT = 6;
    private static final int NEWS_LIMIT = 6;
    private static final int JOB_LIMIT = 6;
    private static final int FORUM_LIMIT = 8;
    private static final int PARTNER_LIMIT = 12;

    private final HomeMapper homeMapper;

    public HomeResponse getHomeData() {
        List<SearchItemVO> microMajors = homeMapper.listMicroMajors(MICRO_LIMIT);
        if (microMajors.isEmpty()) {
            microMajors = homeMapper.listFeaturedCourses(MICRO_LIMIT);
        }

        return HomeResponse.builder()
                .courses(homeMapper.listFeaturedCourses(COURSE_LIMIT))
                .hotProducts(homeMapper.listHotProducts(PRODUCT_LIMIT))
                .hotActivities(homeMapper.listHotActivities(ACTIVITY_LIMIT))
                .microMajors(microMajors)
                .hotNews(homeMapper.listHotNews(NEWS_LIMIT))
                .jobs(homeMapper.listJobs(JOB_LIMIT))
                .forumPosts(homeMapper.listHotForumPosts(FORUM_LIMIT))
                .partners(homeMapper.listPartners(PARTNER_LIMIT))
                .build();
    }
}
