package com.sport.storm.service;

import com.sport.storm.entity.Product;
import java.util.List;

public interface RecommendService {
    /** 为用户推荐商品列表 */
    List<Product> recommend(Long userId, int limit);
}