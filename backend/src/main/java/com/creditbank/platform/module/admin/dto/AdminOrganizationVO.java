package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrganizationVO {
    private Long id;
    private String name;
    private String code;
    private Integer type;
    private String typeName;
    private String contact;
    private String phone;
    private String email;
    private Integer joinStatus;
    private String joinStatusName;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
