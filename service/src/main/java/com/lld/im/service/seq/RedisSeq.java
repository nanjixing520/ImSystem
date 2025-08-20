package com.lld.im.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * ClassName: RedisSeq
 * Package: com.lld.im.service.seq
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/19 上午11:01
 * Version 1.0
 */
@Service
public class RedisSeq {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long doGetSeq(String key){
        return stringRedisTemplate.opsForValue().increment(key);
    }


}