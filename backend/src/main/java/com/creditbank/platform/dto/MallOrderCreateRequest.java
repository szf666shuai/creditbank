package com.creditbank.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MallOrderCreateRequest {

    @Valid
    @NotEmpty(message = "请选择要兑换的商品")
    private List<MallOrderItemRequest> items;

    private Integer payMethod = 1;

    private String remark;
}
