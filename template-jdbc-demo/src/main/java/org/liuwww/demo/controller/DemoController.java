package org.liuwww.demo.controller;

import java.util.Map;

import org.liuwww.db.service.IQueryTemplate;
import org.liuwww.demo.entity.TestUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@EnableCaching
@RestController
public class DemoController
{

    @Autowired
    private IQueryTemplate querytemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Cacheable(cacheNames = "test_user", key = "#id")
    @GetMapping("one")
    public Map<String, Object> getOne(String id)
    {
        // querytemplate.createQueryBean("test_user").getCompare().eq("userId",
        // id).getQueryBean().getMap();
        System.out.println("--query--");
        return querytemplate.getMap("test_user", id);
    }

    @Cacheable(cacheNames = "test_user", key = "#id")
    @GetMapping("getBean")
    public TestUser getBean(String id)
    {
        return new TestUser(Long.parseLong(id), "test", 10);
    }

    @GetMapping("set")
    public String set(String key, String value)
    {
        stringRedisTemplate.opsForValue().set(key, value);
        return "success";
    }

    @GetMapping("get/{key}")
    public String get(@PathVariable String key)
    {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @GetMapping("test")
    public Object test()
    {
        redisTemplate.opsForValue().set("test", new TestUser(11, "test", 10));
        return redisTemplate.opsForValue().get("test");
    }

    @GetMapping("test2")
    public Object test2(String key)
    {
        return redisTemplate.opsForValue().get(key);
    }
}
