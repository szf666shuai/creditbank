package com.creditbank.platform.service;

import com.creditbank.platform.dto.HomeResponse;
import com.creditbank.platform.dto.SearchItemVO;
import com.creditbank.platform.mapper.HomeMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeService.class);

    private static final int COURSE_LIMIT = 8;
    private static final int PRODUCT_LIMIT = 8;
    private static final int ACTIVITY_LIMIT = 6;
    private static final int MICRO_LIMIT = 6;
    private static final int NEWS_LIMIT = 6;
    private static final int JOB_LIMIT = 6;
    private static final int FORUM_LIMIT = 8;
    private static final int PARTNER_LIMIT = 12;

    private final HomeMapper homeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOME_CACHE_KEY = "home:data:v1";
    private static final Duration HOME_CACHE_TTL = Duration.ofMinutes(5);

    public HomeResponse getHomeData() {
        Object cached = redisTemplate.opsForValue().get(HOME_CACHE_KEY);
        if (cached instanceof HomeResponse homeResponse) {
            log.info("[HomeCache] HIT  key={}", HOME_CACHE_KEY);
            return homeResponse;
        }
        log.info("[HomeCache] MISS key={}, rebuild home aggregate", HOME_CACHE_KEY);
        HomeResponse data = buildHomeData();
        redisTemplate.opsForValue().set(HOME_CACHE_KEY, data, HOME_CACHE_TTL);
        return data;
    }

    private HomeResponse buildHomeData() {
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

    /** 数据变更后主动失效首页缓存，下一次访问将自动重建 */
    public void evictHomeCache() {
        Boolean deleted = redisTemplate.delete(HOME_CACHE_KEY);
        log.info("[HomeCache] EVICT key={}, deleted={}", HOME_CACHE_KEY, deleted);
    }
}
