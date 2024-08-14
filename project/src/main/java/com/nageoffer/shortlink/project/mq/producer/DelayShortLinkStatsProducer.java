package com.nageoffer.shortlink.project.mq.producer;

import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟消费短链接统计发送者
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;

    /**
     * 发送延迟消费短链接统计
     *
     * 该方法用于将短链接统计记录发送到延迟队列中，在指定的延迟时间后进行消费处理。
     *
     * @param statsRecord 短链接统计实体参数，包含需要延迟处理的统计数据。
     */
    public void send(ShortLinkStatsRecordDTO statsRecord) {
        // 从 Redisson 客户端中获取一个阻塞队列 (`RBlockingDeque`)，该队列用于存储需要延迟处理的短链接统计记录。
        RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
        // 使用 Redisson 提供的 `getDelayedQueue` 方法，将阻塞队列包装成一个延迟队列 (`RDelayedQueue`)。延迟队列会基于 Redis 的有序集合Zset实现消息的延迟处理。
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        // 将 `statsRecord` 添加到延迟队列中，指定延迟时间为 5 秒。
        delayedQueue.offer(statsRecord, 5, TimeUnit.SECONDS);
    }
}
