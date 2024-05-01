package org.originit.rds;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Slf4j
public class RedisWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisWebApplication.class, args);
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/hello")
    public String hello() {
        return redisTemplate.opsForValue().get("hello");
    }

    @GetMapping("/sayHello")
    public String sayHello(@RequestParam String hello) {
        redisTemplate.opsForValue().set("hello", hello);
        return "success";
    }

    /**
     * 测试1kw 10位数字的键值对使用string存储需要占用的内存
     * 结果为798MB
     * 如果每个键值对请求一次，redis的速度会很慢，因为每次请求都会有一次网络开销，虽然redis性能很好但是一直在等待数据传输过来再执行
     * 所以这里使用multiSet一次性设置20w个键值对
     * 如果对redis的性能不确定，可以使用redis-benchmark [option] [option value]命令进行测试
     */
    @GetMapping("/manyDataInStr")
    public Long manyDataInStr() {
        log.info("start manyDataInStr");
        long start = 1000000000L;
        flushDB();
        Map<String, String>  map = new HashMap<>(200000);
        for (int i = 0; i < 10000000; i++) {
            long cur = ++start;
            map.put(cur + "", cur + "");
            if (i % 200000 == 0) {
                redisTemplate.opsForValue().multiSet(map);
                map.clear();
                log.info("set {} success", i);
            }
        }
        Long usedMemory = getUsedMemory();
        // 异步执行
        CompletableFuture.runAsync(() -> {
            flushDB();
        });
        if (usedMemory == null)
            return 0L;
        return usedMemory / 1024 / 1024;
    }

    /**
     * 测试1kw 10位数字的键值对使用hash存储需要占用的内存
     * 这里将10位数字进行切分，hash key = concat(前七位, 第八位 < 5, '0', '1'),主要目的是让一个hash中最多只有500个元素
     * hash field = 后三位
     * 例如：1234567898 -> key = 12345671, field = 898
     *      1301829345 -> key = 13018290, field = 345
     * 结果为102MB
     */
    @GetMapping("/manyDataInHash")
    public Long manyDataInHash() {
        log.info("start manyDataInHash");
        long start = 1000000000L;
        flushDB();
        for (int i = 0; i <= 10000000; i++) {
            long cur = ++start;
            // 前七位作为hash key， 后三位作为hash field
            // 获取cur前7位
            int c = String.valueOf(cur).charAt(7);
            if (c - '0' < 5) {
                c = 0;
            } else {
                c = 1;
            }
            String key = String.valueOf(cur).substring(0,7) + c;
            String field = String.valueOf(cur).substring(7);
            redisTemplate.opsForHash().put(key, field, cur + "");
            if (i % 100000 == 0) {
                log.info("set {} success", i);
            }
        }
        Long usedMemory = getUsedMemory();
        // 异步执行
        CompletableFuture.runAsync(() -> {
            flushDB();
        });
        if (usedMemory == null)
            return 0L;
        return usedMemory / 1024 / 1024;
    }

    private Long getUsedMemory() {
        Long usedMemory = redisTemplate.execute((RedisCallback<Long>) (connection) -> {
            Properties memory = connection.info("memory");
            if (memory == null || memory.get("used_memory") == null)
                return 0L;
            return Long.parseLong(memory.get("used_memory").toString());
        });
        return usedMemory;
    }

    private void flushDB() {
        redisTemplate.execute((RedisCallback<Object>) (connection) -> {
            connection.flushDb();
            return null;
        });
    }
}
