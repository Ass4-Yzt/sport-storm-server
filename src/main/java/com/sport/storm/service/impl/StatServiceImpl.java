package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.entity.Order;
import com.sport.storm.entity.Product;
import com.sport.storm.entity.User;
import com.sport.storm.mapper.OrderMapper;
import com.sport.storm.mapper.ProductMapper;
import com.sport.storm.mapper.UserMapper;
import com.sport.storm.service.StatService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatServiceImpl implements StatService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public StatServiceImpl(OrderMapper orderMapper, ProductMapper productMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
    }

    @Override
    public BigDecimal totalSales() {
        // 已支付且未取消的订单：status in (1,2,3)
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getStatus, 1, 2, 3));
        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getPayAmount() != null) {
                total = total.add(o.getPayAmount());
            }
        }
        return total;
    }

    @Override
    public long totalOrders() {
        return orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().in(Order::getStatus, 1, 2, 3));
    }

    @Override
    public long totalUsers() {
        return userMapper.selectCount(null);
    }

    @Override
    public List<Map<String, Object>> hotProducts(int limit) {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSales)
                        .last("limit " + limit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("sales", p.getSales());
            m.put("price", p.getPrice());
            result.add(m);
        }
        return result;
    }
}