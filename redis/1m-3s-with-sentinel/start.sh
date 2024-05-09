#!/bin/bash
# 加载环境变量
c_passed=false
# 使用getopts解析命令行选项
while getopts "c" opt; do
  case $opt in
    c)
      c_passed=true
      ;;
  esac
done
docker-compose down
if $c_passed; then
    set -a
    eval $(egrep -v '^#' .env | xargs)
    set +a
    rm -rf sentinel.conf
    # 通过环境变量渲染模版文件
    envsubst < sentinel.conf.template > sentinel.conf
    envsubst < redis.conf.template > redis.conf
    docker-compose down -v 
    docker-compose down --rmi all
fi
docker-compose up -d
if $c_passed; then
    rm -rf sentinel.conf
    rm -rf redis.conf
fi

