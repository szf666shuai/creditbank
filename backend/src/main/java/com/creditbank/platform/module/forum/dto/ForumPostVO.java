package com.creditbank.platform.module.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ForumPostVO {

    private Long id;
    private Long boardId;
    private String boardName;
    private Long userId;
    private String authorName;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer replyCount;
    private Integer likeCount;
    private Integer isTop;
    private Integer status;
    private String statusName;
    private Boolean liked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
