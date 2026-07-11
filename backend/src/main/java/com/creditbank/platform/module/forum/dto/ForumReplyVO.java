package com.creditbank.platform.module.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ForumReplyVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String authorName;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private String statusName;
    private Boolean liked;
    private LocalDateTime createTime;
}
