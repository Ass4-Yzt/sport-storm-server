package com.sport.storm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.storm.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}