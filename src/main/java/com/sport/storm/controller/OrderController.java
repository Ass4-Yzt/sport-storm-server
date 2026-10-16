package com.sport.storm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.common.Result;
import com.sport.storm.entity.Order;
import com.sport.storm.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /** 从购物车下单 */
    @PostMapping("/create")
    public Result<Order> create(HttpServletRequest request,
                                @RequestParam Long addressId,
                                @RequestParam Long[] cartItemIds) {
        return Result.success(orderService.createFromCart(userId(request), addressId, cartItemIds));
    }

    /** 订单详情 */
    @GetMapping("/{orderId}")
    public Result<Order> detail(HttpServletRequest request, @PathVariable Long orderId) {
        return Result.success(orderService.getDetail(userId(request), orderId));
    }

    /** 我的订单列表 */
    @GetMapping("/list")
    public Result<Page<Order>> myOrders(HttpServletRequest request,
                                        @RequestParam(defaultValue = "1") long current,
                                        @RequestParam(defaultValue = "8") long size,
                                        @RequestParam(required = false) Integer status) {
        return Result.success(orderService.myOrders(userId(request), current, size, status));
    }

    /** 模拟支付 */
    @PostMapping("/pay/{orderId}")
    public Result<Void> pay(HttpServletRequest request, @PathVariable Long orderId) {
        orderService.pay(userId(request), orderId);
        return Result.success();
    }

    /** 确认收货 */
    @PostMapping("/confirm/{orderId}")
    public Result<Void> confirm(HttpServletRequest request, @PathVariable Long orderId) {
        orderService.confirm(userId(request), orderId);
        return Result.success();
    }

    /** 取消订单 */
    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancel(HttpServletRequest request, @PathVariable Long orderId) {
        orderService.cancel(userId(request), orderId);
        return Result.success();
    }
}