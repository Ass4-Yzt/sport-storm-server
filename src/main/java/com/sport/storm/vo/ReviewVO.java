package com.sport.storm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {

    private Long id;

    private Long orderId;

    private Long orderItemId;

    private Long productId;

    /** 商品名，管理端列表用 */
    private String productName;

    /** 评价人昵称，查不到时显示“匿名用户” */
    private String nickname;

    private Integer rating;

    private String content;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}