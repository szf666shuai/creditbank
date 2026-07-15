package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileSummaryVO {

    private CreditAccountVO creditAccount;
    private List<LearningArchiveVO> archives;
    private List<LearningCertificateVO> certificates;
}
