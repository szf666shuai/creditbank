package com.creditbank.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    /** 0学员 1企业用户 */
    @NotNull(message = "请选择注册身份")
    private Integer roleType;

    private String phone;
    private String email;

    /** 企业注册必填 */
    private String orgName;
    private String orgCode;
    private String orgIntro;
    private String orgContact;
    private String orgPhone;
}
