package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyForumPostVO {

    private Long id;
    private Long boardId;
    private String boardName;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer replyCount;
    private Integer likeCount;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
