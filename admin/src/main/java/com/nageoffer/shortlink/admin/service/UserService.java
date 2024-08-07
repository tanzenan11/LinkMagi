package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;

/**
 * <p>
 *  用户接口层
 * </p>
 *
 * @author 谭泽楠
 * @since 2024-8-7
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户姓名查询用户
     */
    UserRespDTO getUserByUsername(String username);
}
