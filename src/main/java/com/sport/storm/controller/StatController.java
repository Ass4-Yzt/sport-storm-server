package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.service.StatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stat")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    /** 数据看板核心指标 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalSales", statService.totalSales());
        data.put("totalOrders", statService.totalOrders());
        data.put("totalUsers", statService.totalUsers());
        data.put("hotProducts", statService.hotProducts(5));
        return Result.success(data);
    }
}