-- 为虚拟商品和课程商品增加支付后兑换码，可重复执行
SET NAMES utf8mb4;

SET @add_redemption_code = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_order_item'
     AND COLUMN_NAME = 'redemption_code') = 0,
  'ALTER TABLE mall_order_item ADD COLUMN redemption_code VARCHAR(64) NULL COMMENT ''虚拟商品或课程兑换码'' AFTER price_money',
  'SELECT 1'
);
PREPARE statement FROM @add_redemption_code;
EXECUTE statement;
DEALLOCATE PREPARE statement;

UPDATE mall_order_item item
JOIN mall_order orders ON orders.id = item.order_id AND orders.pay_status = 1
JOIN mall_product product ON product.id = item.product_id
SET item.redemption_code = CONCAT('CB-', UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 12)))
WHERE item.redemption_code IS NULL
  AND product.product_type IN (2, 3);
