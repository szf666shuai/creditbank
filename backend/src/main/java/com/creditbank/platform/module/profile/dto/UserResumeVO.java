package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResumeVO {

    private Long id;
    private String title;
    private ResumeContentVO content;
    private String fileUrl;
    private Integer isDefault;
    private Integer version;
    private LocalDateTime updateTime;
}
