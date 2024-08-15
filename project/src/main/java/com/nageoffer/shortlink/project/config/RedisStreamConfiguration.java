package com.nageoffer.shortlink.project.config;


import com.nageoffer.shortlink.project.mq.consumer.ShortLinkStatsSaveConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis Stream 消息队列配置
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "message-queue", name = "select", havingValue = "redis")
public class RedisStreamConfiguration {

    // Redis 连接工厂，用于创建 Redis 连接
    private final RedisConnectionFactory redisConnectionFactory;

    // 短链接统计数据的消费处理器
    private final ShortLinkStatsSaveConsumer shortLinkStatsSaveConsumer;

    // 从配置文件中获取 Redis 频道的名称 相当于队列的key
    @Value("${spring.data.redis.channel-topic.short-link-stats}")
    private String topic;

    // 从配置文件中获取 Redis 消费者组的名称
    @Value("${spring.data.redis.channel-topic.short-link-stats-group}")
    private String group;

    // 创建异步处理 Stream 消息的线程池
    @Bean
    public ExecutorService asyncStreamConsumer() {
        // 原子整数，用于给线程命名时生成唯一的编号
        AtomicInteger index = new AtomicInteger();
        // 获取当前可用的处理器核心数量
        int processors = Runtime.getRuntime().availableProcessors();
        // 创建一个 ThreadPoolExecutor 线程池
        return new ThreadPoolExecutor(
                processors,                        // 核心线程数，设置为处理器的核心数量
                processors + (processors >> 1),   // 最大线程数，设置为处理器核心数的 1.5 倍（右移一位相当于除以2）
                60,                              // 当线程池中的线程空闲时间超过 60 秒时将其终止
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),    // 使用无界队列来存放等待执行的任务
                // 创建新线程时的工厂方法，设置线程名称并将其设为守护线程
                runnable -> {
                    // 创建一个新线程来运行任务
                    Thread thread = new Thread(runnable);
                    // 设置线程名称，包含自增的编号
                    thread.setName("stream_consumer_short-link_stats_" + index.incrementAndGet());
                    // 将线程设为守护线程，确保在 JVM 关闭时自动退出
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }


    // 创建并配置 StreamMessageListenerContainer，用于监听 Redis Stream 消息
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(ExecutorService asyncStreamConsumer) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 一次最多拉取 10 条消息
                        .batchSize(10)
                        // 指定用于执行任务的线程池 进行绑定
                        .executor(asyncStreamConsumer)
                        // 如果没有拉取到消息，则阻塞的时间，最多 3 秒
                        .pollTimeout(Duration.ofSeconds(3))
                        .build();

        // 创建并绑定 Redis 连接工厂和配置信息，实例化 StreamMessageListenerContainer
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // 配置自动确认消费，指定消费者组、消费者名称和消费处理器
        streamMessageListenerContainer.receiveAutoAck(
                Consumer.from(group, "stats-consumer"),  // 消费者组和消费者名称
                StreamOffset.create(topic, ReadOffset.lastConsumed()),  // 指定从上次消费的位置继续读取
                shortLinkStatsSaveConsumer  // 消息处理逻辑
        );

        // 返回配置好的 StreamMessageListenerContainer 实例
        return streamMessageListenerContainer;
    }

}
