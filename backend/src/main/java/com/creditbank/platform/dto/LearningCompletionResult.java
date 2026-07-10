package com.creditbank.platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LearningCompletionResult {

    private Long userCourseId;
    private LearningCertificateVO certificate;
    private LearningArchiveVO archive;
    private CreditChangeResult creditChange;
}
