package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagVO {

    private Long id;
    private String name;
}
