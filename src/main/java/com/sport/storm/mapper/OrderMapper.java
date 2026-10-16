package com.sport.storm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.storm.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}