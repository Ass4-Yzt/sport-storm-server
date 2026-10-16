package com.sport.storm.service;

import com.sport.storm.vo.ProductReviewVO;
import com.sport.storm.vo.ReviewVO;

import java.util.List;

public interface ReviewService {

    /** 用户提交一条评价：只能评自己的、已完成的订单明细，且不能重复评 */
    void submit(Long userId, Long orderItemId, Integer rating, String content);

    /** 商品详情页：某商品的评价列表与平均分 */
    ProductReviewVO listByProduct(Long productId);

    /** 用户端：某订单下自己已提交的评价，用于回显 */
    List<ReviewVO> listByOrder(Long userId, Long orderId);

    /** 管理端：全部评价 */
    List<ReviewVO> listAll();

    /** 管理端：显示 / 隐藏一条评价 */
    void changeStatus(Long id, Integer status);
}