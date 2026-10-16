package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.entity.MemberLevel;
import com.sport.storm.entity.PointsRecord;
import com.sport.storm.entity.User;
import com.sport.storm.mapper.MemberLevelMapper;
import com.sport.storm.mapper.PointsRecordMapper;
import com.sport.storm.mapper.UserMapper;
import com.sport.storm.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberLevelMapper memberLevelMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final UserMapper userMapper;

    public MemberServiceImpl(MemberLevelMapper memberLevelMapper,
                             PointsRecordMapper pointsRecordMapper,
                             UserMapper userMapper) {
        this.memberLevelMapper = memberLevelMapper;
        this.pointsRecordMapper = pointsRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<MemberLevel> listLevels() {
        return memberLevelMapper.selectList(null);
    }

    @Override
    public MemberLevel calcLevel(BigDecimal totalExpend) {
        // 找出"最低消费要求 <= 累计消费"的最高等级
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .le(MemberLevel::getMinExpend, totalExpend)
                        .orderByDesc(MemberLevel::getMinExpend));
        if (levels.isEmpty()) {
            return memberLevelMapper.selectById(1L);
        }
        return levels.get(0);
    }

    @Override
    @Transactional
    public void refreshLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;
        MemberLevel level = calcLevel(user.getTotalExpend());
        user.setMemberLevelId(level.getId());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void addPoints(Long userId, int points, String source) {
        if (points == 0) return;
        User user = userMapper.selectById(userId);
        if (user == null) return;
        user.setPoints(user.getPoints() + points);
        userMapper.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setSource(source);
        record.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(record);
    }

    @Override
    public List<PointsRecord> pointsRecords(Long userId) {
        return pointsRecordMapper.selectList(
                new LambdaQueryWrapper<PointsRecord>()
                        .eq(PointsRecord::getUserId, userId)
                        .orderByDesc(PointsRecord::getCreateTime));
    }
}