package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyForumReplyVO {

    private Long id;
    private Long postId;
    private String postTitle;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
