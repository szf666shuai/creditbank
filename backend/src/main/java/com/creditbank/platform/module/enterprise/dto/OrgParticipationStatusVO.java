package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrgParticipationStatusVO {

    private List<Long> appliedJobIds;
    private List<Long> registeredActivityIds;
}
