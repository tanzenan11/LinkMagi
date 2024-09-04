技术架构：SpringBoot + Redis + MySQL  + RocketMQ + RabbitMQ + Sentinel + ShardingSphere

项目描述：围绕用户分享视频或商品链接提供便利性，比如分享至微信或抖音等平台时，通过文本加实时短链形式帮助提高传播以及分享功能。系统底层支持高并发短链跳转场景，并发量在本机上达到1800/sec，并提供了深入的分析和跟踪功能，帮助使用者分析用户分享行为以及转化率等。

亮点：
使用布隆过滤器来判断短链接是否已存在，提高了判断效率，并发量相较于使用分布式锁搭配查询数据库方案提升6倍
通过配置灵活切换RocketMQ、RabbitMQ和RedisStream三种消息队列，实现大量访问短链接场景下的监控信息存储的容灾能力，确保系统在高负载条件下依然能够稳定运行
封装缓存不存在读取功能，通过双重判定锁优化更新失效场景下大量查询数据库问题
为保障短链接缓存与数据库之间的数据一致性，采用了通过更新数据库删除缓存的策略，保证了两者之间的数据一致性
在消息队列监控统计业务中，我使用 Redis 来完成幂等场景的处理，确保消息在一定时间内仅被消费一次，避免重复处理
为实现短链接在大量访问场景下的数据修改功能，我使用了 Redisson 分布式读写锁，确保数据修改的安全和一致性
考虑兼容短链接用户需求，短链接数据分片的基础上增加了路由表，使用户能够方便地分页查看短链接
使用布隆过滤器+空值缓存+分布式锁解决短链接跳转缓存穿透问题。

各位假如对代码有一些问题欢迎交流呀，一起进步！！
