package com.sport.storm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_product_tag")
public class ProductTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long tagId;
}