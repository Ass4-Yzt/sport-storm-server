package com.sport.storm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatService {
    /** 总交易额（已支付订单） */
    BigDecimal totalSales();

    /** 订单量（已支付订单数） */
    long totalOrders();

    /** 总用户数 */
    long totalUsers();

    /** 热门商品 Top N（按销量） */
    List<Map<String, Object>> hotProducts(int limit);
}