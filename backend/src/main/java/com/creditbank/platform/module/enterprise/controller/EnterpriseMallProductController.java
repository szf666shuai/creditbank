package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.EnterpriseMallProductSaveRequest;
import com.creditbank.platform.module.enterprise.dto.EnterpriseMallProductVO;
import com.creditbank.platform.module.enterprise.service.EnterpriseMallProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/my/products")
@RequiredArgsConstructor
public class EnterpriseMallProductController {

    private final EnterpriseMallProductService enterpriseMallProductService;

    @GetMapping
    public Result<List<EnterpriseMallProductVO>> listMyProducts() {
        return Result.ok(enterpriseMallProductService.listMyProducts());
    }

    @PostMapping
    public Result<EnterpriseMallProductVO> createProduct(@Valid @RequestBody EnterpriseMallProductSaveRequest request) {
        return Result.ok(enterpriseMallProductService.createProduct(request));
    }

    @PutMapping("/{id}")
    public Result<EnterpriseMallProductVO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody EnterpriseMallProductSaveRequest request) {
        return Result.ok(enterpriseMallProductService.updateProduct(id, request));
    }
}
