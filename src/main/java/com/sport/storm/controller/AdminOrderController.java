package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 管理员：发货 */
    @PostMapping("/ship/{orderId}")
    public Result<Void> ship(@PathVariable Long orderId,
                             @RequestParam String expressCompany,
                             @RequestParam String expressNo) {
        orderService.ship(orderId, expressCompany, expressNo);
        return Result.success();
    }
    /** 管理员：订单列表 */
    @GetMapping("/list")
    public com.sport.storm.common.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.sport.storm.entity.Order>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return com.sport.storm.common.Result.success(orderService.adminListAll(current, size, status));
    }
}