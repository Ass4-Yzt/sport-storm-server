package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.entity.MemberLevel;
import com.sport.storm.entity.PointsRecord;
import com.sport.storm.entity.SportTag;
import com.sport.storm.entity.User;
import com.sport.storm.entity.UserSportProfile;
import com.sport.storm.mapper.MemberLevelMapper;
import com.sport.storm.mapper.SportTagMapper;
import com.sport.storm.mapper.UserMapper;
import com.sport.storm.mapper.UserSportProfileMapper;
import com.sport.storm.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final UserMapper userMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final SportTagMapper sportTagMapper;
    private final UserSportProfileMapper userSportProfileMapper;

    public MemberController(MemberService memberService,
                            UserMapper userMapper,
                            MemberLevelMapper memberLevelMapper,
                            SportTagMapper sportTagMapper,
                            UserSportProfileMapper userSportProfileMapper) {
        this.memberService = memberService;
        this.userMapper = userMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.sportTagMapper = sportTagMapper;
        this.userSportProfileMapper = userSportProfileMapper;
    }

    /** 会员等级列表 */
    @GetMapping("/levels")
    public Result<List<MemberLevel>> levels() {
        return Result.success(memberService.listLevels());
    }

    /** 我的积分流水 */
    @GetMapping("/points")
    public Result<List<PointsRecord>> points(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(memberService.pointsRecords(userId));
    }

    /** 我的会员信息 */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("points", user.getPoints());
        data.put("totalExpend", user.getTotalExpend());
        data.put("memberLevelId", user.getMemberLevelId());
        MemberLevel level = memberLevelMapper.selectById(user.getMemberLevelId());
        data.put("memberLevelName", level != null ? level.getLevelName() : "普通会员");
        return Result.success(data);
    }

    /** 全部运动标签 */
    @GetMapping("/tags")
    public Result<List<SportTag>> tags() {
        List<SportTag> tags = sportTagMapper.selectList(
                new LambdaQueryWrapper<SportTag>().orderByAsc(SportTag::getSort));
        return Result.success(tags);
    }

    /** 我设置的运动偏好 */
    @GetMapping("/preference")
    public Result<List<Long>> preference(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Long> tagIds = userSportProfileMapper.selectList(
                        new LambdaQueryWrapper<UserSportProfile>()
                                .eq(UserSportProfile::getUserId, userId))
                .stream().map(UserSportProfile::getTagId).collect(Collectors.toList());
        return Result.success(tagIds);
    }

    /** 保存运动偏好 */
    @PostMapping("/preference")
    @Transactional
    public Result<Void> savePreference(HttpServletRequest request,
                                       @RequestBody List<Long> tagIds) {
        Long userId = (Long) request.getAttribute("userId");
        // 先清除原设置
        userSportProfileMapper.delete(
                new LambdaQueryWrapper<UserSportProfile>()
                        .eq(UserSportProfile::getUserId, userId));
        // 再写入新设置
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                UserSportProfile profile = new UserSportProfile();
                profile.setUserId(userId);
                profile.setTagId(tagId);
                profile.setPreference(5);
                userSportProfileMapper.insert(profile);
            }
        }
        return Result.success();
    }
}