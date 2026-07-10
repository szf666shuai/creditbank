package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LearningCertificateVO {

    private Long id;
    private String certNo;
    private Long userId;
    private Long courseId;
    private String title;
    private String qrContent;
    private String qrImageUrl;
    private String blockchainHash;
    private Integer verifyStatus;
    private String verifyStatusName;
    private LocalDateTime issuedAt;
}
