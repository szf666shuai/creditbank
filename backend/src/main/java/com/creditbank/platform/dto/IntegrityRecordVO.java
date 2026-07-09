package com.creditbank.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
