package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.entity.Product;
import com.sport.storm.entity.ProductTag;
import com.sport.storm.mapper.ProductMapper;
import com.sport.storm.mapper.ProductTagMapper;
import com.sport.storm.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductTagMapper productTagMapper;

    public ProductServiceImpl(ProductMapper productMapper, ProductTagMapper productTagMapper) {
        this.productMapper = productMapper;
        this.productTagMapper = productTagMapper;
    }

    @Override
    public Page<Product> pageQuery(long current, long size, Long categoryId, String keyword, Long tagId) {
        Page<Product> page = new Page<>(current, size);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查上架商品

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getBrand, keyword)
                    .or().like(Product::getSubtitle, keyword));
        }
        wrapper.orderByDesc(Product::getSales); // 默认按销量

        // 按运动标签筛选：需要关联 t_product_tag
        if (tagId != null) {
            // 查出该标签下的商品 id 列表
            java.util.List<Long> productIds = productTagMapper.selectList(
                    new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getTagId, tagId)
            ).stream().map(ProductTag::getProductId).toList();
            if (productIds.isEmpty()) {
                return page; // 空页
            }
            wrapper.in(Product::getId, productIds);
        }

        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    @Override
    public void add(Product product) {
        product.setId(null);
        if (product.getStock() == null) product.setStock(0);
        if (product.getSales() == null) product.setSales(0);
        product.setStatus(1);
        productMapper.insert(product);
        saveTags(product);
    }

    @Override
    @Transactional
    public void update(Product product) {
        productMapper.updateById(product);
        // 重建商品标签关联
        productTagMapper.delete(
                new LambdaQueryWrapper<ProductTag>().eq(ProductTag::getProductId, product.getId()));
        saveTags(product);
    }

    @Override
    public void delete(Long id) {
        // 逻辑删除：置为下架
        Product p = new Product();
        p.setId(id);
        p.setStatus(0);
        productMapper.updateById(p);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Product p = new Product();
        p.setId(id);
        p.setStatus(status);
        productMapper.updateById(p);
    }

    private void saveTags(Product product) {
        if (product.getTagIds() == null) return;
        for (Long tagId : product.getTagIds()) {
            ProductTag pt = new ProductTag();
            pt.setProductId(product.getId());
            pt.setTagId(tagId);
            productTagMapper.insert(pt);
        }
    }
}