# 目标
实现redis-cluster分片集群，3主3从
# 结构
6个redis实例，通过redis-cli命令组成redis-cluster集群，三主三从
# 启动
- unix
    ```
    sh start.sh
    ```
执行后，需要输入yes确认一下，因为主从都在一个主机上没法实现高可用
# 使用
连接到redis集群中的节点
```
set goods:1001:user:{1} bar
set goods:1002:user:{1} bar
set goods:1003:user:{1} bar
```
能发现通过{1}作为分片的key进行hash，因此这三个key都在同一个节点