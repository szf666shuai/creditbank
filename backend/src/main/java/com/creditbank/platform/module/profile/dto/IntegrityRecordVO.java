package com.creditbank.platform.module.profile.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IntegrityRecordVO {

    private Long id;
    private Integer changeValue;
    private Integer scoreAfter;
    private Integer eventType;
    private String eventTypeName;
    private String reason;
    private String refType;
    private Long refId;
    private LocalDateTime createTime;
}
