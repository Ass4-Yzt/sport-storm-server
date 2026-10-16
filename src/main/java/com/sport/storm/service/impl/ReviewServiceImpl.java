package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.entity.Order;
import com.sport.storm.entity.OrderItem;
import com.sport.storm.entity.Product;
import com.sport.storm.entity.Review;
import com.sport.storm.entity.User;
import com.sport.storm.mapper.OrderItemMapper;
import com.sport.storm.mapper.OrderMapper;
import com.sport.storm.mapper.ProductMapper;
import com.sport.storm.mapper.ReviewMapper;
import com.sport.storm.mapper.UserMapper;
import com.sport.storm.service.ReviewService;
import com.sport.storm.vo.ProductReviewVO;
import com.sport.storm.vo.ReviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long userId, Long orderItemId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("请选择1到5星的评分");
        }
        if (content != null && content.length() > 500) {
            throw new RuntimeException("评价内容不能超过500字");
        }

        OrderItem item = orderItemMapper.selectById(orderItemId);
        if (item == null) {
            throw new RuntimeException("订单明细不存在");
        }

        // 归属校验：只能评自己的订单
        Order order = orderMapper.selectById(item.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("无权评价该订单");
        }
        // 只有已完成的订单可以评价
        if (order.getStatus() == null || order.getStatus() != 3) {
            throw new RuntimeException("订单完成后才能评价");
        }
        // 防重复评价
        Long count = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>().eq(Review::getOrderItemId, orderItemId));
        if (count != null && count > 0) {
            throw new RuntimeException("该商品已评价，不能重复评价");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setOrderItemId(orderItemId);
        review.setProductId(item.getProductId());
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content == null ? null : content.trim());
        review.setStatus(1);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    @Override
    public ProductReviewVO listByProduct(Long productId) {
        List<Review> list = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreateTime)
                        .orderByDesc(Review::getId));

        ProductReviewVO vo = new ProductReviewVO();
        vo.setTotal(list.size());
        double sum = 0;
        for (Review r : list) {
            sum += r.getRating() == null ? 0 : r.getRating();
        }
        double avg = list.isEmpty() ? 0.0 : sum / list.size();
        vo.setAvgRating(Math.round(avg * 10) / 10.0);
        vo.setReviews(toVOList(list, false));
        return vo;
    }

    @Override
    public List<ReviewVO> listByOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        List<Review> list = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, orderId)
                        .eq(Review::getUserId, userId));
        return toVOList(list, false);
    }

    @Override
    public List<ReviewVO> listAll() {
        List<Review> list = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .orderByDesc(Review::getCreateTime)
                        .orderByDesc(Review::getId));
        return toVOList(list, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("状态取值只能是0或1");
        }
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        review.setStatus(status);
        reviewMapper.updateById(review);
    }

    private List<ReviewVO> toVOList(List<Review> list, boolean withProductName) {
        List<ReviewVO> result = new ArrayList<>();
        if (list.isEmpty()) {
            return result;
        }

        // 批次查昵称，避免循环里单条查库
        Map<Long, String> nicknameMap = new HashMap<>();
        Set<Long> userIds = list.stream()
                .map(Review::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            for (User u : userMapper.selectBatchIds(userIds)) {
                String nickname = u.getNickname();
                nicknameMap.put(u.getId(),
                        (nickname == null || nickname.trim().isEmpty()) ? "匿名用户" : nickname);
            }
        }

        Map<Long, String> productMap = new HashMap<>();
        if (withProductName) {
            Set<Long> productIds = list.stream()
                    .map(Review::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!productIds.isEmpty()) {
                for (Product p : productMapper.selectBatchIds(productIds)) {
                    productMap.put(p.getId(), p.getName());
                }
            }
        }

        for (Review r : list) {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);
            vo.setNickname(nicknameMap.getOrDefault(r.getUserId(), "匿名用户"));
            if (withProductName) {
                vo.setProductName(productMap.get(r.getProductId()));
            }
            result.add(vo);
        }
        return result;
    }
}