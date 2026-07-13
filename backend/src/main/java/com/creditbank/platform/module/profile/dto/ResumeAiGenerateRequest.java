package com.creditbank.platform.module.profile.dto;

import lombok.Data;

@Data
public class ResumeAiGenerateRequest {

    /** 目标岗位或方向，如 Java 后端开发 */
    private String targetRole;

    /** 补充说明，如突出项目经历、应届生等 */
    private String extraHint;
}
