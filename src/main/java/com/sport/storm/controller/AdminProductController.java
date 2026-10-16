package com.sport.storm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.common.Result;
import com.sport.storm.entity.Product;
import com.sport.storm.entity.ProductTag;
import com.sport.storm.entity.SportTag;
import com.sport.storm.mapper.ProductMapper;
import com.sport.storm.mapper.ProductTagMapper;
import com.sport.storm.mapper.SportTagMapper;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    private final ProductMapper productMapper;
    private final ProductTagMapper productTagMapper;
    private final SportTagMapper sportTagMapper;

    public AdminProductController(ProductMapper productMapper,
                                  ProductTagMapper productTagMapper,
                                  SportTagMapper sportTagMapper) {
        this.productMapper = productMapper;
        this.productTagMapper = productTagMapper;
        this.sportTagMapper = sportTagMapper;
    }

    /** 商品列表（含下架商品，供后台管理） */
    @GetMapping("/list")
    public Result<Page<Product>> list(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        return Result.success(productMapper.selectPage(page, wrapper));
    }

    /** 全部运动标签（新增商品时勾选用） */
    @GetMapping("/tags")
    public Result<List<SportTag>> tags() {
        return Result.success(sportTagMapper.selectList(
                new LambdaQueryWrapper<SportTag>().orderByAsc(SportTag::getSort)));
    }

    /** 新增商品（同时保存运动标签关联） */
    @PostMapping("/add")
    @Transactional
    public Result<Void> add(@RequestBody ProductForm form) {
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            return Result.error("商品名称不能为空");
        }
        if (form.getPrice() == null) {
            return Result.error("请填写售价");
        }

        Product p = new Product();
        p.setName(form.getName().trim());
        p.setBrand(form.getBrand());
        p.setSubtitle(form.getSubtitle());
        p.setCategoryId(form.getCategoryId() == null ? 1L : form.getCategoryId());
        p.setPrice(form.getPrice());
        p.setOriginalPrice(form.getOriginalPrice());
        p.setStock(form.getStock() == null ? 0 : form.getStock());
        p.setStockWarning(5);
        p.setSales(0);
        p.setImage(form.getImage());
        p.setDetail(form.getDetail());
        p.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        p.setIsHot(form.getIsHot() == null ? 0 : form.getIsHot());
        p.setCreateTime(LocalDateTime.now());
        productMapper.insert(p);

        // 保存运动标签关联——推荐功能的数据来源
        if (form.getTagIds() != null) {
            for (Long tagId : form.getTagIds()) {
                ProductTag pt = new ProductTag();
                pt.setProductId(p.getId());
                pt.setTagId(tagId);
                productTagMapper.insert(pt);
            }
        }
        return Result.success();
    }

    /** 上架 / 下架 */
    @PostMapping("/status/{id}")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            return Result.error("商品不存在");
        }
        p.setStatus(status);
        productMapper.updateById(p);
        return Result.success();
    }

    /** 新增商品的表单对象 */
    @Data
    public static class ProductForm {
        private String name;
        private String brand;
        private String subtitle;
        private Long categoryId;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
        private String image;
        private String detail;
        private Integer status;
        private Integer isHot;
        private List<Long> tagIds;
    }
}