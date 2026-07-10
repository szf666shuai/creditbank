package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateVerifyResult {

    private boolean valid;
    private String certNo;
    private String title;
    private String blockchainHash;
    private LocalDateTime issuedAt;
    private String message;
}
