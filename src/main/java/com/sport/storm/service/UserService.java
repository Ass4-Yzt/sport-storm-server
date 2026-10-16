package com.sport.storm.service;

import com.sport.storm.entity.User;

public interface UserService {
    /** 注册 */
    void register(User user);

    /** 登录，成功返回 token */
    String login(String username, String password);
}