FROM redis:5.0.14
LABEL author="xxcisbest"
COPY ./redis.conf /etc/redis/redis.conf
ENTRYPOINT ["redis-server", "/etc/redis/redis.conf" ]
