package org.originit.rds;

import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
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
}
