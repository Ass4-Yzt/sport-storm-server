package com.sport.storm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_sport_tag")
public class SportTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;
    private String tagIcon;
    private String description;
    private Integer sort;
}