package com.sport.storm.service;

import com.sport.storm.entity.MemberLevel;
import com.sport.storm.entity.PointsRecord;
import com.sport.storm.entity.User;
import java.math.BigDecimal;
import java.util.List;

public interface MemberService {
    /** 获取所有会员等级 */
    List<MemberLevel> listLevels();

    /** 按累计消费额计算用户应得的等级 */
    MemberLevel calcLevel(BigDecimal totalExpend);

    /** 刷新用户等级（消费后调用） */
    void refreshLevel(Long userId);

    /** 下单支付后加积分（1元=1积分） */
    void addPoints(Long userId, int points, String source);

    /** 查询积分流水 */
    List<PointsRecord> pointsRecords(Long userId);
}