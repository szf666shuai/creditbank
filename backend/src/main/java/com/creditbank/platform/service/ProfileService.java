package com.creditbank.platform.service;

import com.creditbank.platform.dto.ProfileSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CreditService creditService;
    private final LearningService learningService;

    public ProfileSummaryVO getSummary(Long userId) {
        return ProfileSummaryVO.builder()
                .creditAccount(creditService.getAccount(userId))
                .archives(learningService.listArchives(userId, 8))
                .certificates(learningService.listCertificates(userId, 8))
                .build();
    }
}
