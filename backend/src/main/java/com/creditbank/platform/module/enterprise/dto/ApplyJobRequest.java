package com.creditbank.platform.module.enterprise.dto;

import lombok.Data;

@Data
public class ApplyJobRequest {

    private String coverMessage;

    /** 指定简历 ID；不传则使用默认简历 */
    private Long resumeId;

    /** 为 false 时不附带任何简历 */
    private Boolean attachResume;
}
