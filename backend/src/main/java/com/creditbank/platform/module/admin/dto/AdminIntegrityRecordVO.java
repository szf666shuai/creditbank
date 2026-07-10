package com.creditbank.platform.module.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminIntegrityRecordVO {
    private Long id;
    private Long userId;
    private String userName;
    private Integer changeValue;
    private Integer scoreAfter;
    private Integer eventType;
    private String eventTypeName;
    private String reason;
    private String refType;
    private Long refId;
    private LocalDateTime createTime;
}
