package com.creditbank.platform.module.enterprise.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.module.enterprise.dto.MaterialManageVO;
import com.creditbank.platform.module.enterprise.dto.MaterialSaveRequest;
import com.creditbank.platform.module.enterprise.service.EnterpriseMaterialService;
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
@RequestMapping("/api/enterprise/my/materials")
@RequiredArgsConstructor
public class EnterpriseMaterialController {

    private final EnterpriseMaterialService enterpriseMaterialService;

    @GetMapping
    public Result<List<MaterialManageVO>> listMyMaterials() {
        return Result.ok(enterpriseMaterialService.listMyMaterials());
    }

    @PostMapping
    public Result<MaterialManageVO> createMaterial(@Valid @RequestBody MaterialSaveRequest request) {
        return Result.ok(enterpriseMaterialService.createMaterial(request));
    }

    @PutMapping("/{id}")
    public Result<MaterialManageVO> updateMaterial(@PathVariable Long id, @Valid @RequestBody MaterialSaveRequest request) {
        return Result.ok(enterpriseMaterialService.updateMaterial(id, request));
    }

    @PostMapping("/{id}/offline")
    public Result<Void> offlineMaterial(@PathVariable Long id) {
        enterpriseMaterialService.offlineMaterial(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/online")
    public Result<Void> onlineMaterial(@PathVariable Long id) {
        enterpriseMaterialService.onlineMaterial(id);
        return Result.ok(null);
    }
}
