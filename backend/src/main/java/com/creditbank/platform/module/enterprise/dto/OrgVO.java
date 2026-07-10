package com.creditbank.platform.module.enterprise.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgVO {

    private Long id;
    private String name;
    private String code;
    private Integer type;
    private String typeName;
    private String logo;
    private String intro;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String website;
}
