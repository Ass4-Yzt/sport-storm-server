package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.entity.Product;
import com.sport.storm.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    /** 首页"为你推荐" */
    @GetMapping("/list")
    public Result<List<Product>> recommend(HttpServletRequest request,
                                           @RequestParam(defaultValue = "8") int limit) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.recommend(userId, limit));
    }
}