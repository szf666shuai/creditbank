package com.creditbank.platform.controller;

import com.creditbank.platform.common.Result;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.dto.MallCategoryVO;
import com.creditbank.platform.dto.MallOrderCreateRequest;
import com.creditbank.platform.dto.MallOrderVO;
import com.creditbank.platform.dto.MallProductVO;
import com.creditbank.platform.dto.PaymentResultVO;
import com.creditbank.platform.service.MallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    @GetMapping("/categories")
    public Result<List<MallCategoryVO>> categories() {
        return Result.ok(mallService.listCategories());
    }

    @GetMapping("/products")
    public Result<List<MallProductVO>> products(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String keyword) {
        return Result.ok(mallService.listProducts(categoryId, keyword));
    }

    @GetMapping("/products/{productId}")
    public Result<MallProductVO> product(@PathVariable Long productId) {
        return Result.ok(mallService.getProduct(productId));
    }

    @PostMapping("/orders")
    public Result<MallOrderVO> createOrder(@Valid @RequestBody MallOrderCreateRequest request) {
        return Result.ok(mallService.createOrder(UserContext.getUserId(), request));
    }

    @GetMapping("/orders")
    public Result<List<MallOrderVO>> orders(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(mallService.listOrders(UserContext.getUserId(), limit));
    }

    @GetMapping("/orders/{orderId}")
    public Result<MallOrderVO> order(@PathVariable Long orderId) {
        return Result.ok(mallService.getOrder(UserContext.getUserId(), orderId));
    }

    @PostMapping("/orders/{orderId}/pay")
    public Result<PaymentResultVO> pay(@PathVariable Long orderId) {
        return Result.ok(mallService.payOrder(UserContext.getUserId(), orderId));
    }
}
