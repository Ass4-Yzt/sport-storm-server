package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.service.AdminService;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** 管理员登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        String token = adminService.login(req.getUsername(), req.getPassword());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", req.getUsername());
        return Result.success(data);
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}