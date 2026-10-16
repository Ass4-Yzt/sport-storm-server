package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.entity.CartItem;
import com.sport.storm.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /** 加购 */
    @PostMapping("/add")
    public Result<Void> add(HttpServletRequest request,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity) {
        cartService.add(userId(request), productId, quantity);
        return Result.success();
    }

    /** 购物车列表 */
    @GetMapping("/list")
    public Result<List<CartItem>> list(HttpServletRequest request) {
        return Result.success(cartService.list(userId(request)));
    }

    /** 修改数量 */
    @PutMapping("/quantity")
    public Result<Void> updateQuantity(HttpServletRequest request,
                                       @RequestParam Long cartItemId,
                                       @RequestParam Integer quantity) {
        cartService.updateQuantity(userId(request), cartItemId, quantity);
        return Result.success();
    }

    /** 勾选/取消 */
    @PutMapping("/selected")
    public Result<Void> updateSelected(HttpServletRequest request,
                                       @RequestParam Long cartItemId,
                                       @RequestParam Integer selected) {
        cartService.updateSelected(userId(request), cartItemId, selected);
        return Result.success();
    }

    /** 删除单项 */
    @DeleteMapping("/{cartItemId}")
    public Result<Void> remove(HttpServletRequest request, @PathVariable Long cartItemId) {
        cartService.remove(userId(request), cartItemId);
        return Result.success();
    }

    /** 清空 */
    @DeleteMapping("/clear")
    public Result<Void> clear(HttpServletRequest request) {
        cartService.clear(userId(request));
        return Result.success();
    }
}