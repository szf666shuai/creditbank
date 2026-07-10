package com.creditbank.platform.module.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMyOrgRequest {

    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称过长")
    private String name;

    @Size(max = 500, message = "简介过长")
    private String intro;

    @Size(max = 50, message = "联系人过长")
    private String contact;

    @Size(max = 20, message = "联系电话过长")
    private String phone;

    @Size(max = 100, message = "邮箱过长")
    private String email;

    @Size(max = 200, message = "地址过长")
    private String address;

    @Size(max = 200, message = "网站过长")
    private String website;

    @Size(max = 255, message = "Logo 地址过长")
    private String logo;
}
