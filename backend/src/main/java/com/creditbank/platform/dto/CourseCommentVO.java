package com.creditbank.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CourseCommentVO {

    private Long id;
    private Long courseId;
    private Long userId;
    private Long parentId;
    private String authorName;
    private String replyToAuthorName;
    private String avatar;
    private String content;
    private Integer likeCount;
    private Boolean liked;
    private BigDecimal creditReward;
    private LocalDateTime createTime;
    private List<CourseCommentVO> replies = new ArrayList<>();
}
