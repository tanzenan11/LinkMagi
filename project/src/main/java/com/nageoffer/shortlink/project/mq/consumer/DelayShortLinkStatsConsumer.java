package com.nageoffer.shortlink.project.mq.consumer;

import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟记录短链接统计组件
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;

    public void onMessage() {
        // 创建一个单线程的线程池，用于消费延迟队列中的消息
        Executors.newSingleThreadExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_stats_consumer");
                            thread.setDaemon(Boolean.TRUE); // 设置为守护线程，使得 JVM 在退出时无需等待此线程
                            return thread;
                        })
                .execute(() -> {
                    // 获取阻塞队列，用于存储延迟队列的消息
                    RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
                    // 将阻塞队列包装为延迟队列，用于延迟处理消息
                    RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
                    // 无限循环，持续从延迟队列中获取并处理消息
                    for (; ; ) {
                        try {
                            // 从延迟队列中获取一条消息，若没有消息则返回 null
                            ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            if (statsRecord != null) {
                                // 如果获取到的消息不为空，则调用短链接服务处理统计信息
                                shortLinkService.shortLinkStats(null, null, statsRecord);
                                continue; // 继续下一次循环，处理下一个消息
                            }
                            // 如果没有消息，则休眠 500 毫秒后继续检查队列
                            LockSupport.parkUntil(500);
                        } catch (Throwable ignored) {
                            // 捕获并忽略所有异常，保证循环不会因为异常而中断
                        }
                    }
                });
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}

