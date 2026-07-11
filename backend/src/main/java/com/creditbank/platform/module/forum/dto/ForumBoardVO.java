package com.creditbank.platform.module.forum.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumBoardVO {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Long postCount;
}
