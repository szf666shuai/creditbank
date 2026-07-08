package com.creditbank.platform.service;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.SearchItemVO;
import com.creditbank.platform.dto.SearchResponse;
import com.creditbank.platform.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_LIMIT = 50;
    private static final Map<String, String> TYPE_NAMES = Map.of(
            "course", "课程",
            "resource", "学习资源",
            "forum", "论坛",
            "news", "资讯",
            "activity", "活动",
            "job", "招聘",
            "enterprise", "企业",
            "credit", "积分商品"
    );

    private final SearchMapper searchMapper;

    public SearchResponse search(String keyword, String type, int limit) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(400, "请输入搜索关键词");
        }
        String q = keyword.trim();
        int pageLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        String searchType = StringUtils.hasText(type) ? type.trim() : "all";

        List<SearchItemVO> items = switch (searchType) {
            case "course" -> searchMapper.searchCourses(q, pageLimit);
            case "resource" -> merge(
                    searchMapper.searchCourses(q, pageLimit),
                    searchMapper.searchResources(q, pageLimit),
                    pageLimit
            );
            case "forum" -> searchMapper.searchForum(q, pageLimit);
            case "news" -> searchMapper.searchNews(q, pageLimit);
            case "activity" -> searchMapper.searchActivities(q, pageLimit);
            case "job" -> searchMapper.searchJobs(q, pageLimit);
            case "enterprise" -> searchMapper.searchEnterprises(q, pageLimit);
            case "credit" -> searchMapper.searchCreditProducts(q, pageLimit);
            case "all" -> searchAll(q, pageLimit);
            default -> throw new BusinessException(400, "不支持的搜索分类");
        };

        items.forEach(item -> item.setTypeName(TYPE_NAMES.getOrDefault(item.getType(), "其他")));

        return SearchResponse.builder()
                .keyword(q)
                .type(searchType)
                .total(items.size())
                .items(items)
                .build();
    }

    private List<SearchItemVO> searchAll(String keyword, int limit) {
        int eachLimit = Math.max(limit / 3, 5);
        Map<Long, SearchItemVO> dedup = new LinkedHashMap<>();
        List<List<SearchItemVO>> groups = List.of(
                searchMapper.searchCourses(keyword, eachLimit),
                searchMapper.searchActivities(keyword, eachLimit),
                searchMapper.searchForum(keyword, eachLimit),
                searchMapper.searchNews(keyword, eachLimit),
                searchMapper.searchJobs(keyword, eachLimit),
                searchMapper.searchCreditProducts(keyword, eachLimit),
                searchMapper.searchResources(keyword, eachLimit),
                searchMapper.searchEnterprises(keyword, eachLimit)
        );
        for (List<SearchItemVO> group : groups) {
            for (SearchItemVO item : group) {
                long key = (item.getType().hashCode() * 31L) + item.getId();
                dedup.putIfAbsent(key, item);
            }
        }
        return dedup.values().stream()
                .sorted(Comparator.comparing(SearchItemVO::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    private List<SearchItemVO> merge(List<SearchItemVO> a, List<SearchItemVO> b, int limit) {
        List<SearchItemVO> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged.stream()
                .sorted(Comparator.comparing(SearchItemVO::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }
}
