package com.sport.storm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.entity.Product;

public interface ProductService {
    /** 分页查询：可按分类、关键词、运动标签筛选 */
    Page<Product> pageQuery(long current, long size, Long categoryId, String keyword, Long tagId);

    /** 商品详情 */
    Product getById(Long id);

    /** 管理员：新增商品 */
    void add(Product product);

    /** 管理员：修改商品 */
    void update(Product product);

    /** 管理员：删除商品（逻辑：置为下架） */
    void delete(Long id);

    /** 商品上下架 */
    void changeStatus(Long id, Integer status);
}