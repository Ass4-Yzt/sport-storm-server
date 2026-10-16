package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.entity.CartItem;
import com.sport.storm.entity.Product;
import com.sport.storm.mapper.CartItemMapper;
import com.sport.storm.mapper.ProductMapper;
import com.sport.storm.service.CartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    public void add(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() == 0) {
            throw new RuntimeException("商品已下架");
        }

        // 已存在则累加数量
        CartItem exist = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getProductId, productId));
        if (exist != null) {
            int newQty = exist.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                throw new RuntimeException("超过库存数量");
            }
            exist.setQuantity(newQty);
            cartItemMapper.updateById(exist);
        } else {
            if (quantity > product.getStock()) {
                throw new RuntimeException("超过库存数量");
            }
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setSelected(1);
            item.setAddTime(LocalDateTime.now());
            // 加购有效期：7天后失效（开题里的"超时未结算自动失效"）
            item.setExpireTime(LocalDateTime.now().plusDays(7));
            cartItemMapper.insert(item);
        }
    }

    @Override
    public List<CartItem> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getAddTime));
        // 填充商品信息
        for (CartItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) {
                item.setProductName(p.getName());
                item.setProductImage(p.getImage());
                item.setProductPrice(p.getPrice());
                item.setProductStock(p.getStock());
                item.setProductStatus(p.getStatus());
            }
        }
        return items;
    }

    @Override
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = getOwned(userId, cartItemId);
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        Product p = productMapper.selectById(item.getProductId());
        if (p != null && quantity > p.getStock()) {
            throw new RuntimeException("超过库存数量");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    @Override
    public void updateSelected(Long userId, Long cartItemId, Integer selected) {
        CartItem item = getOwned(userId, cartItemId);
        item.setSelected(selected);
        cartItemMapper.updateById(item);
    }

    @Override
    public void remove(Long userId, Long cartItemId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getId, cartItemId)
                        .eq(CartItem::getUserId, userId));
    }

    @Override
    public void clear(Long userId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    /** 校验购物车条目归属当前用户 */
    private CartItem getOwned(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new RuntimeException("购物车条目不存在");
        }
        return item;
    }
}