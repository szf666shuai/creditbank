package com.creditbank.platform.module.forum.dto;

import lombok.Data;

@Data
public class CreateForumPostRequest {

    private Long boardId;
    private String title;
    private String content;
}
