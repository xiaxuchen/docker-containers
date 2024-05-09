# 介绍
- 基于spring-data-redis和redission接入redis sentinel集群
- 测试对比string和hash分别存储10位id的映射的内存
# 启动
```
sh start.sh
```
# 测试
- 成功运行
```
curl http://localhost:8080/hello
```
- 测试string存储
```
curl http://localhost:8080/manyDataInStr
```
- 测试hash存储
```
curl http://localhost:8080/manyDataInHash
```