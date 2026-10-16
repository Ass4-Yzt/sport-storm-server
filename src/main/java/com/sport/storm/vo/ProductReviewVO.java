package com.sport.storm.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProductReviewVO {

    /** 平均分，保留一位小数，无评价时为 0.0 */
    private Double avgRating;

    /** 评价条数 */
    private Integer total;

    private List<ReviewVO> reviews;
}