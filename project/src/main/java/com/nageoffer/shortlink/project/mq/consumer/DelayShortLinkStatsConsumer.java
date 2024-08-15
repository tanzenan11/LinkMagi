package com.nageoffer.shortlink.project.mq.consumer;

import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

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
                            // 从延迟队列中获取一条消息
                            ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            //判断消息是否存在
                            if (statsRecord != null) {
                                //消息存在
                                if (!messageQueueIdempotentHandler.isMessageProcessed(statsRecord.getKeys())) { //判断消息是否被消费
                                    // 判断当前的这个消息流程是否执行完成 保证由于异常情况下未删除幂等标识或者未设置完成情况依旧保证幂等
                                    if (messageQueueIdempotentHandler.isAccomplish(statsRecord.getKeys())) {
                                        return;
                                    }
                                    throw new ServiceException("消息未完成流程，需要消息队列重试");
                                }
                                try {
                                    shortLinkService.shortLinkStats(null, null, statsRecord);
                                } catch (Throwable ex) {
                                    // 某某某情况宕机了，删除幂等标识
                                    messageQueueIdempotentHandler.delMessageProcessed(statsRecord.getKeys());
                                    log.error("延迟记录短链接监控消费异常", ex);
                                }
                                // 设置消息流程执行完成
                                messageQueueIdempotentHandler.setAccomplish(statsRecord.getKeys());
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

