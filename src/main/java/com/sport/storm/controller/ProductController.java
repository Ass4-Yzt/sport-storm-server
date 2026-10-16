package com.sport.storm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.common.Result;
import com.sport.storm.entity.Product;
import com.sport.storm.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 前台：商品分页列表（分类/关键词/标签筛选） */
    @GetMapping("/page")
    public Result<Page<Product>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "8") long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId) {
        return Result.success(productService.pageQuery(current, size, categoryId, keyword, tagId));
    }

    /** 前台：商品详情 */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /** 后台：新增商品 */
    @PostMapping("/admin")
    public Result<Void> add(@RequestBody Product product) {
        productService.add(product);
        return Result.success();
    }

    /** 后台：修改商品 */
    @PutMapping("/admin")
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success();
    }

    /** 后台：删除商品（下架） */
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    /** 后台：上下架 */
    @PutMapping("/admin/status/{id}")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.changeStatus(id, status);
        return Result.success();
    }
}