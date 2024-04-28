@echo off
REM 加载.env文件

for /F "tokens=*" %%A in ('findstr /v /r /c:"^#" .env') do (
    set %%A
)

docker-compose down -v
docker-compose up -d redis-master
echo Waiting for master data generating...

REM 重新生成数据
docker exec -it redis-master redis-cli -a %REDIS_PASSWORD% flushall
docker exec -it redis-master redis-cli -a %REDIS_PASSWORD% eval "for i=1,2000000 do redis.call('SET', 'key'..i, 'abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz'..i) end" 0
echo Master data generated
docker-compose down
echo slave starting...
docker-compose up -d
echo slave started