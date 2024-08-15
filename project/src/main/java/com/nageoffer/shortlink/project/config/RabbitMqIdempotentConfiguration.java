package com.nageoffer.shortlink.project.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringAMQP的MessageConverter生成唯一消息ID保证幂等性
 */
@Configuration
@ConditionalOnProperty(prefix = "message-queue", name = "select", havingValue = "mq")
public class RabbitMqIdempotentConfiguration {

    /**
     * 生成唯一消息id
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        // 创建 Jackson2JsonMessageConverter 实例
        Jackson2JsonMessageConverter jjmc = new Jackson2JsonMessageConverter();
        // 启用消息 ID 自动生成
        jjmc.setCreateMessageIds(true);
        return jjmc;
    }

}
