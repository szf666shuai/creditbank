package com.creditbank.platform.module.information.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InformationItemVO {

    private String type;
    private Long id;
    private Long orgId;
    private String orgName;
    private String title;
    private String summary;
    private String location;
    private String tag;
    private Integer status;
    private String statusName;
    private LocalDateTime startTime;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
