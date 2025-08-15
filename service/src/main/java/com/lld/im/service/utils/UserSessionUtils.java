package com.lld.im.service.utils;

import com.alibaba.fastjson.JSON;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ClassName: UserSessionUtils
 * Package: com.lld.im.service.utils
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/6 下午3:54
 * Version 1.0
 */
@Component
public class UserSessionUtils {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //1.获取用户所有的session
    public List<UserSession> getUserSession(Integer appId,String userId) {
        String userSessionKey=appId+ Constants.RedisConstants.UserSessionConstants+userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);
        ArrayList<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object value : values) {
            String str=(String)value;
            UserSession userSession= JSON.parseObject(str,UserSession.class);
            if(userSession.getConnectState().equals(ImConnectStatusEnum.ONLINE_STATUS.getCode())){
                list.add(userSession);
            }
        }
        return list;
    }
    //2.获取用户除了本端的session(用到再来补充，可能会直接在上层方法中过滤)
    //3.获取用户指定端的session
    public UserSession getUserSession(Integer appId,String userId,Integer clientType,String imei){
        String userSessionKey=appId+ Constants.RedisConstants.UserSessionConstants+userId;
        String hashKey=clientType+":"+imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession userSession= JSON.parseObject(o.toString(),UserSession.class);
        return userSession;
    }


}
