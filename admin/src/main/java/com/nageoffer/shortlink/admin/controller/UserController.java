package com.nageoffer.shortlink.admin.controller;


import cn.hutool.core.bean.BeanUtil;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  用户管理控制层
 * </p>
 *
 * @author 谭泽楠
 * @since 2024-8-7
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO result = userService.getUserByUsername(username);
        return Results.success(result);
    }

    /**
     * 根据用户名查找用户无脱敏信息
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<ActualUserRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        UserRespDTO result = userService.getUserByUsername(username);
        return Results.success(BeanUtil.copyProperties(result, ActualUserRespDTO.class));
    }
}
