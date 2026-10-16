package com.sport.storm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("t_shipping")
public class Shipping {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String expressCompany;
    private String expressNo;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 非表字段：物流轨迹 */
    @TableField(exist = false)
    private List<ShippingTrace> traces;
}