package com.creditbank.platform.dto;

import lombok.Data;

@Data
public class CourseMaterialVO {

    private Long id;
    private Long courseId;
    private String title;
    private String fileType;
    private String fileUrl;
    private Integer sortOrder;
}
