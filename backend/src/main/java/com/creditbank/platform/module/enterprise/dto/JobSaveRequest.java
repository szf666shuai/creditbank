package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class JobSaveRequest {

    @NotBlank(message = "职位名称不能为空")
    private String title;

    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
    private String eduRequirement;
    private List<Long> tagIds;
}
