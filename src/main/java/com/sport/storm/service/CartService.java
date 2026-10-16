package com.sport.storm.service;

import com.sport.storm.entity.CartItem;
import java.util.List;

public interface CartService {
    /** 加购 */
    void add(Long userId, Long productId, Integer quantity);

    /** 购物车列表（带商品信息） */
    List<CartItem> list(Long userId);

    /** 修改数量 */
    void updateQuantity(Long userId, Long cartItemId, Integer quantity);

    /** 勾选/取消勾选 */
    void updateSelected(Long userId, Long cartItemId, Integer selected);

    /** 删除单项 */
    void remove(Long userId, Long cartItemId);

    /** 清空购物车 */
    void clear(Long userId);
}