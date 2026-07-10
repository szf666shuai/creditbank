package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResumeSummaryVO {

    private Long id;
    private String title;
    private String realName;
    private Integer isDefault;
    private Integer version;
    private LocalDateTime updateTime;
}
