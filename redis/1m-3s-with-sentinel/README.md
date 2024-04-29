# 目标
- 基于1m3s架构增加sentinel监控
- 对主从复制添加复制限制(从机都离线禁止写，主从延迟超过限制等待)
# 结构
- 1主3从3哨兵，客观下线qurom为2
- 由于需要主从切换，主从的密码设置的是一样的
- 哨兵设置了密码
- 哨兵的配置通过脚本加载.env环境变量进行渲染后打包成镜像
- 为了本机开发能够从sentinel获取到可连接的地址，配置节点的replica-announce-ip和replica-announce-port,通过start.sh脚本获取了本机ip并注入到了配置文件中
# 重要配置
sentinel.conf
```
# 配置监控的master
sentinel monitor mymaster redis-master 6379 2
# 主观下线时间
sentinel down-after-milliseconds mymaster 6000
# 故障转移超时时间
sentinel failover-timeout mymaster 30000
# 最多一个从节点进行故障转移为master，使得该从节点不可用
sentinel parallel-syncs mymaster 1
# master密码
sentinel auth-pass mymaster ${REDIS_PASSWORD}
# 哨兵密码
requirepass ${REDIS_PASSWORD}
```
redis.conf
```
# 至少要有一个从节点才能写
min-replicas-to-write 1
# 主从延迟要小于10s才能写
min-replicas-max-lag 10
```
# 使用
- unix
    ```
    sh start.sh -c
    ```
    其中-c将清空相关镜像重新构建
# 官方文档
[点此跳转](https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/)