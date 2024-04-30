#!/bin/bash 
# docker compose down -v 
# docker compose up --build -d
# 加载.env环境变量
set -a
    eval $(egrep -v '^#' .env | xargs)
set +a
# 创建分片集群
docker exec -it redis-node7000 redis-cli -a $REDIS_PASSWORD --cluster create \
    127.0.0.1:7000 127.0.0.1:7001 \
    127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 \
    --cluster-replicas 1