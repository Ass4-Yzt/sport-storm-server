package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.entity.*;
import com.sport.storm.mapper.*;
import com.sport.storm.service.RecommendService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    private final UserSportProfileMapper userSportProfileMapper;
    private final ProductTagMapper productTagMapper;
    private final ProductMapper productMapper;
    private final UserBehaviorMapper userBehaviorMapper;

    public RecommendServiceImpl(UserSportProfileMapper userSportProfileMapper,
                                ProductTagMapper productTagMapper,
                                ProductMapper productMapper,
                                UserBehaviorMapper userBehaviorMapper) {
        this.userSportProfileMapper = userSportProfileMapper;
        this.productTagMapper = productTagMapper;
        this.productMapper = productMapper;
        this.userBehaviorMapper = userBehaviorMapper;
    }

    @Override
    public List<Product> recommend(Long userId, int limit) {
        // 1. 获取用户设置的运动标签
        List<UserSportProfile> profiles = userSportProfileMapper.selectList(
                new LambdaQueryWrapper<UserSportProfile>()
                        .eq(UserSportProfile::getUserId, userId));
        Set<Long> userTagIds = profiles.stream()
                .map(UserSportProfile::getTagId)
                .collect(Collectors.toSet());

        // 2. 行为推断：近30天内某运动标签关联的商品被访问满5次，则并入用户兴趣
        List<UserBehavior> recent = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .ge(UserBehavior::getBehaviorTime, LocalDateTime.now().minusDays(30)));
        Map<Long, Integer> tagHits = new HashMap<>();
        for (UserBehavior b : recent) {
            List<Long> tags = productTagMapper.selectList(
                            new LambdaQueryWrapper<ProductTag>()
                                    .eq(ProductTag::getProductId, b.getProductId()))
                    .stream().map(ProductTag::getTagId).collect(Collectors.toList());
            for (Long t : tags) {
                tagHits.merge(t, 1, Integer::sum);
            }
        }
        for (Map.Entry<Long, Integer> e : tagHits.entrySet()) {
            if (e.getValue() >= 5) {
                userTagIds.add(e.getKey());
            }
        }

        // 3. 内容匹配：计算每个商品与用户标签的重合数量作为匹配分
        Map<Long, Integer> productScore = new HashMap<>();
        List<Product> allProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));

        for (Product p : allProducts) {
            List<Long> tagIds = productTagMapper.selectList(
                            new LambdaQueryWrapper<ProductTag>()
                                    .eq(ProductTag::getProductId, p.getId()))
                    .stream().map(ProductTag::getTagId).collect(Collectors.toList());
            if (!userTagIds.isEmpty()) {
                long overlap = tagIds.stream().filter(userTagIds::contains).count();
                if (overlap > 0) {
                    productScore.put(p.getId(), (int) overlap);
                }
            }
        }

        // 4. 协同过滤：相似用户买过、而我没买过的商品追加得分
        Set<Long> similarUserIds = new HashSet<>();
        if (!userTagIds.isEmpty()) {
            List<UserSportProfile> allProfiles = userSportProfileMapper.selectList(null);
            for (UserSportProfile sp : allProfiles) {
                if (!sp.getUserId().equals(userId) && userTagIds.contains(sp.getTagId())) {
                    similarUserIds.add(sp.getUserId());
                }
            }
        }

        // 只排除我"已购买"的商品（浏览过的仍可推荐）
        Set<Long> myBought = userBehaviorMapper.selectList(
                        new LambdaQueryWrapper<UserBehavior>()
                                .eq(UserBehavior::getUserId, userId)
                                .eq(UserBehavior::getBehaviorType, 5))
                .stream().map(UserBehavior::getProductId).collect(Collectors.toSet());

        if (!similarUserIds.isEmpty()) {
            List<UserBehavior> similarBehaviors = userBehaviorMapper.selectList(
                    new LambdaQueryWrapper<UserBehavior>()
                            .in(UserBehavior::getUserId, similarUserIds)
                            .eq(UserBehavior::getBehaviorType, 5));
            for (UserBehavior ub : similarBehaviors) {
                if (myBought.contains(ub.getProductId())) continue;
                productScore.merge(ub.getProductId(), 1, Integer::sum);
            }
        }

        // 5. 排序：按综合得分降序，同分按销量降序
        return allProducts.stream()
                .filter(p -> productScore.containsKey(p.getId())
                        || (p.getIsHot() != null && p.getIsHot() == 1))
                .sorted(Comparator
                        .comparingInt((Product p) -> productScore.getOrDefault(p.getId(), 0)).reversed()
                        .thenComparing(Product::getSales, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}