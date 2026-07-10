package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumeContentVO {

    private String realName;
    private String phone;
    private String email;
    private String education;
    private String workExperience;
    private String skills;
    private String selfIntro;
    private String projects;
}
