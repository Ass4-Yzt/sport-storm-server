package com.sport.storm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.storm.entity.Address;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}