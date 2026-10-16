package com.sport.storm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.common.Result;
import com.sport.storm.entity.Shipping;
import com.sport.storm.mapper.ShippingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipping")
public class AdminShippingController {

    @Autowired
    private ShippingMapper shippingMapper;

    @GetMapping("/list")
    public Result list() {
        List<Shipping> list = shippingMapper.selectList(
                new LambdaQueryWrapper<Shipping>().orderByDesc(Shipping::getUpdateTime));
        return Result.success(list);
    }
}