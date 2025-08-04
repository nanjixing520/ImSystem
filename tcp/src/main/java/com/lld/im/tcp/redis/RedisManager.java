package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.receiver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

/**
 * ClassName: RedisManager
 * Package: com.lld.im.tcp.redis
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/30 上午11:44
 * Version 1.0
 */
public class RedisManager {
    private static RedissonClient redissonClient;
    private static Integer loginModel;
    public static void init(BootstrapConfig config){
        loginModel=config.getLim().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());
        UserLoginMessageListener userLoginMessageListener=new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();
    }
    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }
}
