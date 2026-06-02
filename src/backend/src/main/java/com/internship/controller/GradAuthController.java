package com.internship.controller;

import com.internship.common.Result;
import com.internship.dto.GradLoginRequest;
import com.internship.dto.GradLoginResponse;
import com.internship.entity.User;
import com.internship.service.GradAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "毕设认证管理")
public class GradAuthController {

    private final GradAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<GradLoginResponse> login(@RequestBody GradLoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @PostMapping("/activate")
    @Operation(summary = "账号激活")
    public Result<Void> activate(@RequestParam Integer userId, 
                                  @RequestParam String phone,
                                  @RequestParam String password) {
        authService.activate(userId, phone, password);
        return Result.success();
    }

    @PostMapping("/switch-role")
    @Operation(summary = "角色切换")
    public Result<GradLoginResponse> switchRole(@RequestParam Integer roleId) {
        return Result.success(authService.switchRole(roleId));
    }
}
