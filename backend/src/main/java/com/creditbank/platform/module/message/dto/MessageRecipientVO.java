package com.creditbank.platform.module.message.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageRecipientVO {

    private Long id;
    private String username;
    private String realName;
    private String displayName;
    private Integer role;
    private String roleName;
}
