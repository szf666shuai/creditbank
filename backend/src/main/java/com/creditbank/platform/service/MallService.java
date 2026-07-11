package com.creditbank.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.dto.CreditChangeResult;
import com.creditbank.platform.dto.CreditSpendRequest;
import com.creditbank.platform.dto.MallCategoryVO;
import com.creditbank.platform.dto.MallOrderCreateRequest;
import com.creditbank.platform.dto.MallOrderItemRequest;
import com.creditbank.platform.dto.MallOrderItemVO;
import com.creditbank.platform.dto.MallOrderVO;
import com.creditbank.platform.dto.MallProductVO;
import com.creditbank.platform.dto.PaymentResultVO;
import com.creditbank.platform.entity.MallCategory;
import com.creditbank.platform.entity.MallOrder;
import com.creditbank.platform.entity.MallOrderItem;
import com.creditbank.platform.entity.MallProduct;
import com.creditbank.platform.entity.PaymentRecord;
import com.creditbank.platform.entity.UserCourse;
import com.creditbank.platform.mapper.MallCategoryMapper;
import com.creditbank.platform.mapper.MallOrderItemMapper;
import com.creditbank.platform.mapper.MallOrderMapper;
import com.creditbank.platform.mapper.MallProductMapper;
import com.creditbank.platform.mapper.PaymentRecordMapper;
import com.creditbank.platform.mapper.UserCourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MallService {

    private static final DateTimeFormatter NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MallCategoryMapper categoryMapper;
    private final MallProductMapper productMapper;
    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper orderItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final CreditService creditService;
    private final UserCourseMapper userCourseMapper;

    public List<MallCategoryVO> listCategories() {
        return categoryMapper.selectList(
                        new LambdaQueryWrapper<MallCategory>()
                                .eq(MallCategory::getStatus, 1)
                                .orderByAsc(MallCategory::getSortOrder)
                                .orderByAsc(MallCategory::getId)
                )
                .stream()
                .map(c -> MallCategoryVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .parentId(c.getParentId())
                        .sortOrder(c.getSortOrder())
                        .build())
                .toList();
    }

    public List<MallProductVO> listProducts(Long categoryId, String keyword) {
        List<MallProductVO> products = productMapper.listProducts(categoryId, trim(keyword));
        products.forEach(p -> p.setProductTypeName(productTypeName(p.getProductType())));
        return products;
    }

    public MallProductVO getProduct(Long productId) {
        MallProductVO product = productMapper.getProduct(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在或已下架");
        }
        product.setProductTypeName(productTypeName(product.getProductType()));
        return product;
    }

    @Transactional
    public MallOrderVO createOrder(Long userId, MallOrderCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("请选择要兑换的商品");
        }
        MallOrder order = new MallOrder();
        order.setOrderNo(nextNo("MO"));
        order.setUserId(userId);
        order.setTotalCredit(BigDecimal.ZERO);
        order.setTotalMoney(BigDecimal.ZERO);
        order.setPayMethod(request.getPayMethod() == null ? 1 : request.getPayMethod());
        order.setPayStatus(0);
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalMoney = BigDecimal.ZERO;
        for (MallOrderItemRequest itemRequest : request.getItems()) {
            MallProduct product = requireProduct(itemRequest.getProductId());
            int quantity = itemRequest.getQuantity() == null ? 1 : itemRequest.getQuantity();
            if (quantity < 1) {
                throw new BusinessException("商品数量至少为 1");
            }
            if (product.getStock() != null && product.getStock() < quantity) {
                throw new BusinessException(product.getName() + " 库存不足");
            }
            MallOrderItem item = new MallOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(quantity);
            item.setPriceCredit(nz(product.getPriceCredit()));
            item.setPriceMoney(nz(product.getPriceMoney()));
            orderItemMapper.insert(item);

            totalCredit = totalCredit.add(nz(product.getPriceCredit()).multiply(BigDecimal.valueOf(quantity)));
            totalMoney = totalMoney.add(nz(product.getPriceMoney()).multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalCredit(totalCredit);
        order.setTotalMoney(totalMoney);
        orderMapper.updateById(order);
        return toOrderVO(order);
    }

    @Transactional
    public PaymentResultVO payOrder(Long userId, Long orderId) {
        MallOrder order = requireOrder(userId, orderId);
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            PaymentRecord existing = paymentRecordMapper.selectOne(
                    new LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getOrderId, orderId)
                            .last("LIMIT 1")
            );
            return PaymentResultVO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .payNo(existing == null ? null : existing.getPayNo())
                    .amountCredit(order.getTotalCredit())
                    .amountMoney(order.getTotalMoney())
                    .build();
        }

        List<MallOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<MallOrderItem>()
                        .eq(MallOrderItem::getOrderId, orderId)
                        .orderByAsc(MallOrderItem::getId)
        );
        for (MallOrderItem item : orderItems) {
            MallProduct product = requireProduct(item.getProductId());
            if (product.getStock() != null
                    && productMapper.decreaseStock(product.getId(), item.getQuantity()) != 1) {
                throw new BusinessException(product.getName() + " 库存不足，无法完成支付");
            }
            if (product.getProductType() != null && product.getProductType() == 3
                    && product.getRefCourseId() != null) {
                startPurchasedCourse(userId, product.getRefCourseId());
            }
            if (product.getProductType() != null && (product.getProductType() == 2 || product.getProductType() == 3)
                    && !StringUtils.hasText(item.getRedemptionCode())) {
                item.setRedemptionCode(nextRedemptionCode());
                orderItemMapper.updateById(item);
            }
        }

        CreditChangeResult creditChange = null;
        if (nz(order.getTotalCredit()).compareTo(BigDecimal.ZERO) > 0) {
            CreditSpendRequest spendRequest = new CreditSpendRequest();
            spendRequest.setAmount(order.getTotalCredit());
            spendRequest.setBizType("mall_order");
            spendRequest.setRefType("mall_order");
            spendRequest.setRefId(order.getId());
            spendRequest.setSource("积分商城订单支付: " + order.getOrderNo());
            creditChange = creditService.spend(userId, spendRequest);
        }

        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        PaymentRecord payment = new PaymentRecord();
        payment.setOrderId(order.getId());
        payment.setUserId(userId);
        payment.setPayNo(nextNo("PAY"));
        payment.setAmountCredit(order.getTotalCredit());
        payment.setAmountMoney(order.getTotalMoney());
        payment.setPayChannel("mock");
        payment.setPayStatus(1);
        paymentRecordMapper.insert(payment);

        return PaymentResultVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .payNo(payment.getPayNo())
                .amountCredit(order.getTotalCredit())
                .amountMoney(order.getTotalMoney())
                .creditChange(creditChange)
                .build();
    }

    public List<MallOrderVO> listOrders(Long userId, int limit) {
        return orderMapper.selectList(
                        new LambdaQueryWrapper<MallOrder>()
                                .eq(MallOrder::getUserId, userId)
                                .orderByDesc(MallOrder::getCreateTime)
                                .last("LIMIT " + Math.min(Math.max(limit, 1), 50))
                )
                .stream()
                .map(this::toOrderVO)
                .toList();
    }

    public MallOrderVO getOrder(Long userId, Long orderId) {
        return toOrderVO(requireOrder(userId, orderId));
    }

    private MallOrder requireOrder(Long userId, Long orderId) {
        MallOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private MallProduct requireProduct(Long productId) {
        MallProduct product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(404, "商品不存在或已下架");
        }
        return product;
    }

    public MallOrderVO toOrderVO(MallOrder order) {
        List<MallOrderItemVO> items = new ArrayList<>();
        if (order.getId() != null) {
            items = orderItemMapper.selectList(
                            new LambdaQueryWrapper<MallOrderItem>()
                                    .eq(MallOrderItem::getOrderId, order.getId())
                                    .orderByAsc(MallOrderItem::getId)
                    )
                    .stream()
                    .map(item -> {
                        MallProduct product = productMapper.selectById(item.getProductId());
                        return MallOrderItemVO.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .productType(product == null ? null : product.getProductType())
                                .refCourseId(product == null ? null : product.getRefCourseId())
                                .quantity(item.getQuantity())
                                .priceCredit(item.getPriceCredit())
                                .priceMoney(item.getPriceMoney())
                                .redemptionCode(item.getRedemptionCode())
                                .build();
                    })
                    .toList();
        }
        return MallOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .totalCredit(order.getTotalCredit())
                .totalMoney(order.getTotalMoney())
                .payMethod(order.getPayMethod())
                .payMethodName(payMethodName(order.getPayMethod()))
                .payStatus(order.getPayStatus())
                .payStatusName(payStatusName(order.getPayStatus()))
                .payTime(order.getPayTime())
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .items(items)
                .build();
    }

    private String nextNo(String prefix) {
        return prefix + LocalDateTime.now().format(NO_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String nextRedemptionCode() {
        return "CB-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
    }

    private void startPurchasedCourse(Long userId, Long courseId) {
        Long existing = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId)
        );
        if (existing != null && existing > 0) {
            return;
        }
        UserCourse record = new UserCourse();
        record.setUserId(userId);
        record.setCourseId(courseId);
        record.setProgress(0);
        record.setWatchedSeconds(0);
        record.setMaxWatchedPositionSeconds(0);
        record.setLastPositionSeconds(0);
        record.setStatus(0);
        record.setPaidCredit(BigDecimal.ZERO);
        userCourseMapper.insert(record);
    }

    private String productTypeName(Integer type) {
        if (type == null) return "其他";
        return switch (type) {
            case 1 -> "实物";
            case 2 -> "虚拟";
            case 3 -> "课程";
            case 4 -> "服务";
            default -> "其他";
        };
    }

    private String payMethodName(Integer method) {
        if (method == null) return "秩点支付";
        return switch (method) {
            case 2 -> "模拟支付";
            case 3 -> "混合支付";
            default -> "秩点支付";
        };
    }

    private String payStatusName(Integer status) {
        if (status == null) return "待支付";
        return switch (status) {
            case 1 -> "已支付";
            case 2 -> "已取消";
            case 3 -> "已退款";
            default -> "待支付";
        };
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
