package com.sport.storm.service;

public interface AdminService {
    /** 管理员登录，成功返回 token */
    String login(String username, String password);
}