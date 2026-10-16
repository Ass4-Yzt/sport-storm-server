package com.sport.storm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.entity.Order;

public interface OrderService {

    /** 从购物车选中的商品下单 */
    Order createFromCart(Long userId, Long addressId, Long[] cartItemIds);

    /** 订单详情（含明细、物流） */
    Order getDetail(Long userId, Long orderId);

    /** 我的订单分页（按状态筛选） */
    Page<Order> myOrders(Long userId, long current, long size, Integer status);

    /** 模拟支付 */
    void pay(Long userId, Long orderId);

    /** 管理员：发货 */
    void ship(Long orderId, String expressCompany, String expressNo);

    /** 确认收货 */
    void confirm(Long userId, Long orderId);

    /** 取消订单 */
    void cancel(Long userId, Long orderId);
    /** 管理员：查询所有订单（分页，可按状态筛选） */
    Page<Order> adminListAll(long current, long size, Integer status);
}