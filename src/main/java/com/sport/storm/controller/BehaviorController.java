package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.entity.UserBehavior;
import com.sport.storm.mapper.UserBehaviorMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/behavior")
public class BehaviorController {

    private final UserBehaviorMapper userBehaviorMapper;

    public BehaviorController(UserBehaviorMapper userBehaviorMapper) {
        this.userBehaviorMapper = userBehaviorMapper;
    }

    /** 记录行为：type 1=浏览 4=加购 5=购买 */
    @PostMapping("/record")
    public Result<Void> record(HttpServletRequest request,
                               @RequestParam Long productId,
                               @RequestParam(defaultValue = "1") Integer type) {
        Long userId = (Long) request.getAttribute("userId");
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(type);
        behavior.setBehaviorTime(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);
        return Result.success();
    }
}