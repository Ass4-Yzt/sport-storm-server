package com.sport.storm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.storm.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}