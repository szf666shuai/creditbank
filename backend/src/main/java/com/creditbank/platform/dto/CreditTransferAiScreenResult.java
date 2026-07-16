package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditTransferAiScreenResult {

    /** approve / reject / uncertain */
    private String suggestion;
    private String reason;
    /** 是否真正调用了大模型；false 表示降级启发式 */
    private boolean llmUsed;
}
