# 目标
- 实现redis主从复制
- 验证redis多slave首次psync时会进行一次rdb，其他slave等待rdb
- 配置混合aof日志进行持久化
# 结构
采用1主3从的结构，且master和slave都设置了密码，通过.env文件作为环境变量，通过docker-compose.yml配置容器、网络、卷等信息，通过start.sh进行数据的生成并启动整个redis集群。
# 重要配置
1. redis主从复制
采用`--replicaof ip port`作为redis-server命令的参数，令slave同步master的进度
2. 验证redis多slave首次psync时会进行一次rdb，其他slave等待rdb
`docker compose up -d`之后`docker compose logs redis-master`查看master日志，可以看到如下内容：
    ```
    redis-master  | 1:M 28 Apr 2024 05:25:06.570 * Replica 172.21.0.3:6379 asks for synchronization
    redis-master  | 1:M 28 Apr 2024 05:25:06.570 * Full resync requested by replica 172.21.0.3:6379
    redis-master  | 1:M 28 Apr 2024 05:25:06.570 * Starting BGSAVE for SYNC with target: disk
    redis-master  | 1:M 28 Apr 2024 05:25:06.666 * Background saving started by pid 36
    redis-master  | 1:M 28 Apr 2024 05:25:06.720 * Replica 172.21.0.5:6379 asks for synchronization
    redis-master  | 1:M 28 Apr 2024 05:25:06.720 * Full resync requested by replica 172.21.0.5:6379
    redis-master  | 1:M 28 Apr 2024 05:25:06.720 * Waiting for end of BGSAVE for SYNC
    redis-master  | 1:M 28 Apr 2024 05:25:06.721 * Replica 172.21.0.4:6379 asks for synchronization
    redis-master  | 1:M 28 Apr 2024 05:25:06.721 * Full resync requested by replica 172.21.0.4:6379
    redis-master  | 1:M 28 Apr 2024 05:25:06.721 * Waiting for end of BGSAVE for SYNC
    redis-master  | 36:C 28 Apr 2024 05:25:12.913 * DB saved on disk
    redis-master  | 36:C 28 Apr 2024 05:25:12.951 * RDB: 1 MB of memory used by copy-on-write
    redis-master  | 1:M 28 Apr 2024 05:25:13.023 * Background saving terminated with success
    redis-master  | 1:M 28 Apr 2024 05:25:14.302 * Synchronization with replica 172.21.0.5:6379 succeeded
    redis-master  | 1:M 28 Apr 2024 05:25:14.319 * Synchronization with replica 172.21.0.4:6379 succeeded
    redis-master  | 1:M 28 Apr 2024 05:25:14.344 * Synchronization with replica 172.21.0.3:6379 succeeded
    ```

    - 可以看到第1个slave启动子进程bgsave后，其他slave再进行首次同步就会等待已有的rdb，当rdb完成后，就会将rdb数据发送给slave完成同步。因此不会多个rdb进程同时进行导致cow(Copy On Write)内存大大增加。

    - 当然rdb采用fork子进程cow机制，当有大量写入的时候，可能会导致内存占用飙升，而导致物理内存不足从而触发内存淘汰机制或者使用swap导致性能下降(redis不应开启swap分区)

3. 配置混合aof日志进行持久化
    ```
    # 开启aof日志，默认no
    appendonly yes
    # 每秒刷盘，如果不想数据丢失，则使用always
    appendfsync everysec
    # aof重写使用rdb混合
    aof-use-rdb-preamble yes
    ```

# 启动
- unix
    ```
    sh start.sh
    ```
- windows 
    ```
    start.bat
    ```