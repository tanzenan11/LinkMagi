Project description: Provide convenience around user sharing video or product links, such as when sharing to WeChat or Jittery Voice and other platforms, through the text plus real-time short chain form to help improve the dissemination as well as the sharing function. The bottom layer of the system supports high concurrency short-chain hopping scenarios, with a throughput of 2600/sec on the local machine, and provides in-depth analysis and tracking functions to help users analyze user sharing behaviors and
conversion rate, etc

Use Bloom filters to determine whether a short link already exists or not, which improves the judgment efficiency and is far better than using distributed locks with database querying.
Flexible switching of RocketMQ, RabbitMQ, and RedisStream message queues to achieve disaster recovery for the storage of monitoring information under the scenario of a large number of accessed short links, and to ensure that the system can still run stably under high load conditions.
Ensure stable operation of the system under high load conditions.
Encapsulate the non-existent read function of the cache, and optimize the problem of mass querying the database under the scenario of update or invalidation by double judgment lock.
To ensure data consistency between the short-link cache and the database, the strategy of deleting the cache by updating the database is adopted to ensure data consistency between the two.
In the message queue consumption business, I use Redis to complete the idempotent scenario to ensure that the message is consumed only once in a certain period of time to avoid repeated processing.
In order to realize the data modification function of short links in a large number of access scenarios, I use Redisson distributed read and write locks to ensure the security and consistency of data modification.
Considering compatibility with short link user needs, short link data slicing based on the addition of a routing table, so that users can conveniently view the short link paging
Use Bloom filter + null value cache + distributed lock to solve the short link jump cache penetration problem.
