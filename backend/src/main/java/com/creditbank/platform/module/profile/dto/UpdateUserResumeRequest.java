package com.creditbank.platform.module.profile.dto;

import lombok.Data;

@Data
public class UpdateUserResumeRequest {

    private String title;
    private ResumeContentVO content;
    private String fileUrl;
}
