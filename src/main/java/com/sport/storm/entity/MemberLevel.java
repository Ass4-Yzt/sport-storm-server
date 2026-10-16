package com.sport.storm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_member_level")
public class MemberLevel {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String levelName;
    private BigDecimal minExpend;
    private BigDecimal discount;
    private String description;
}