# 集群配置计划
## Docker-compose快速开发集群
- [x] [redis主从集群](./redis/1m-3s/README.md)
- [x] [redis主从加哨兵高可用集群](./redis/1m-3s-with-sentinel/README.md)
- [x] [redis分片集群](./redis/cluster/README.md)
- [ ] mysql主从集群
- [ ] kafka高可用集群
- [ ] rocketmq高可用集群

# 问题思考
## 关于docker虚拟网卡与服务发现的思考
对于redis、kafka、rocketmq等中间件，进行服务注册时节点会自动从网卡获取ip，这会使得获取到虚拟网卡的内网ip，因此无法正常通信。
**解决办法:**
- 修改容器的端口配置，容器直接使用宿主机的网络，`--network host`
- 手动修改容器内服务的配置文件，让服务返回固定的宿主机的ip地址
## 宿主机是否可通过容器ip访问容器？
**可以**，如果容器使用默认的network(bridge)或者用户自定义的network并自动分配了ip地址，那么宿主机就能够通过分配的ip访问容器(正常不考虑防火墙规则的情况下)。
但是，**在mac上，不能够通过容器的ip访问容器，因为mac是通过虚拟机虚拟了一个linux系统，并在该系统上运行docker，因此容器的宿主机并不是mac**，
而**在windows上，其能够ping通windows容器，但是ping不通linux容器**