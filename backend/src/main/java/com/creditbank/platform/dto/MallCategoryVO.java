package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MallCategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
}
