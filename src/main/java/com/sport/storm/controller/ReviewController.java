package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /** 用户提交评价 */
    @PostMapping("/submit/{orderItemId}")
    public Result<Void> submit(HttpServletRequest request,
                               @PathVariable Long orderItemId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String content) {
        reviewService.submit(userId(request), orderItemId, rating, content);
        return Result.success();
    }

    /** 商品详情页：评价列表与平均分，游客可看 */
    @GetMapping("/product/{productId}")
    public Result<Object> listByProduct(@PathVariable Long productId) {
        return Result.success(reviewService.listByProduct(productId));
    }

    /** 某订单下自己已提交的评价 */
    @GetMapping("/order/{orderId}")
    public Result<Object> listByOrder(HttpServletRequest request, @PathVariable Long orderId) {
        return Result.success(reviewService.listByOrder(userId(request), orderId));
    }
}