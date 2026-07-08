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
public class SearchItemVO {
    private String type;
    private String typeName;
    private Long id;
    private String title;
    private String summary;
    private String extra;
    private LocalDateTime createTime;
}
