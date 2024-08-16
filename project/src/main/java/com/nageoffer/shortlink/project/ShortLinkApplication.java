
package com.nageoffer.shortlink.project;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * <p>
 *  启动类
 * </p>
 *
 * @author 谭泽楠
 * @since 2024-8-7
 */
@SpringBootApplication
@MapperScan("com.nageoffer.shortlink.project.dao.mapper")
@EnableScheduling
@EnableDiscoveryClient
public class ShortLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApplication.class, args);
    }
}
