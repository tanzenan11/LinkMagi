
package com.nageoffer.shortlink.admin;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * <p>
 *  启动类
 * </p>
 *
 * @author 谭泽楠
 * @since 2024-8-7
 */
@SpringBootApplication
@MapperScan("com.nageoffer.shortlink.admin.dao.mapper")
public class ShortLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
