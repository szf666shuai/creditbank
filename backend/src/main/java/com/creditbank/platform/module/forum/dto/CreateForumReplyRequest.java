package com.creditbank.platform.module.forum.dto;

import lombok.Data;

@Data
public class CreateForumReplyRequest {

    private Long parentId;
    private String content;
}
