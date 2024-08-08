package com.nageoffer.shortlink.admin.dto.resp;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 *  用户返回参数响应
 * </p>
 *
 * @author 谭泽楠
 * @since 2024-8-7
 */
@Data
@Accessors(chain = true)
public class ActualUserRespDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;


    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */

    private String phone;

    /**
     * 邮箱
     */
    private String mail;

}
