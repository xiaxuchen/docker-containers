#!/bin/bash
# 加载环境变量
set -a
eval $(egrep -v '^#' .env | xargs)
set +a
rm -rf sentinel.conf
# 通过环境变量渲染模版文件
envsubst < sentinel.conf.template > sentinel.conf
docker-compose down -v
docker rmi -f redis-sentinel > /dev/null
docker-compose up -d
rm -rf sentinel.conf