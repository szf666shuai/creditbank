package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MaterialSaveRequest {

    @NotBlank(message = "资料标题不能为空")
    @Size(max = 200, message = "标题过长")
    private String title;

    @Size(max = 2000, message = "描述过长")
    private String description;

    @NotBlank(message = "文件链接不能为空")
    @Size(max = 255, message = "文件链接过长")
    private String fileUrl;

    @NotNull(message = "资料类型不能为空")
    private Integer materialType;
}
