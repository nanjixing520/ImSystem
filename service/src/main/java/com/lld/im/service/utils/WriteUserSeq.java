package com.lld.im.service.utils;

import com.lld.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * ClassName: WriteUserSeq
 * Package: com.lld.im.service.utils
 * Description:
 * 将用户的各数据序列号写入redis中
 * @Author 南极星
 * @Create 2025/8/21 下午5:54
 * Version 1.0
 */
@Service
public class WriteUserSeq {

    //redis
    //uid friend 10
    //    friendReq 12
    //    conversation 123
    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId,String userId,String type,Long seq){
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }


}