package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer role;
    private String roleName;
    private Long orgId;
    private String orgName;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
