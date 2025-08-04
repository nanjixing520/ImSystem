package com.lld.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: SessionSocketHolder
 * Package: com.lld.im.tcp.utils
 * Description:
 *       SessionSocketHolder 是一个用于存储和管理会话（Session）与套接字（Socket）关联关系的类。它是一个工具类或组件，主要功能是维护用户会话与网络连接之间的映射
 *       在即时通讯系统中，将用户 ID（会话标识）映射到对应的网络连接（Socket 或 Channel），以便向特定用户发送消息。
 *       在分布式系统中，跟踪客户端会话的状态和对应的网络连接。
 * @Author 南极星
 * @Create 2025/7/29 上午11:28
 * Version 1.0
 */
public class SessionSocketHolder {
    //ConcurrentHashMap 是 Java 提供的线程安全哈希表,实现高效并发，允许多个线程同时读写
    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();
    public static NioSocketChannel get(Integer appId,String userId,Integer clientType,String imei) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        userClientDto.setAppId(appId);
        userClientDto.setImei(imei);
        return CHANNELS.get(userClientDto);
    }

    /**
     * 获得同一个app的某一用户的所有连接通道
     * @param appId
     * @param id
     * @return
     */
    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel ->{
            if(channel.getAppId().equals(appId) && id.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }
    public static void put(Integer appId,String userId,Integer clientType ,String imei,
                           NioSocketChannel channel) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        userClientDto.setImei(imei);
        CHANNELS.put(userClientDto, channel);
    }
    public static void remove(Integer appId,String userId,Integer clientType,String imei){
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setUserId(userId);
        userClientDto.setImei(imei);
        CHANNELS.remove(userClientDto);
    }

    /**
     * 通过 Channel 对象反查并删除对应的键值对
     * 删除这段映射关系
     * @param channel
     */
    public static void remove(NioSocketChannel channel){
/**
 * entrySet()用于将 Map 中的所有键值对 转换为一个 Set 集合，其中每个元素都是一个 Map.Entry 对象（表示一个键值对）
 *      Map<K, V> 转换为 Set<Map.Entry<K, V>>，便于遍历或流式处理
 * stream() 将集合转换为流，便于过滤。
 * filter(...) 通过值（Channel 对象）筛选出目标键值对。
 * forEach(...) 通过键删除对应的映射。
 */
        CHANNELS.entrySet().stream()
                .filter(entity->entity.getValue()==channel)
                .forEach(entity->CHANNELS.remove(entity.getKey()));
    }

    /**
     * 退出登录
     * @param channel
     */
    public static void removeUserSession(NioSocketChannel channel){
        //删除session（内存中的channel）
        //获取这个通道关联所需的属性
        String userId = (String)channel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) channel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) channel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId,userId,clientType,imei);
        //删除redis中的路由关系
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String,String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType+":"+imei);
        channel.close();
    }

    /**
     * 退出后台
     * @param channel
     */
    public static void offlineUserSession(NioSocketChannel channel) {
        //删除session（内存中的channel）
        String userId = (String)channel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) channel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) channel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId,userId,clientType,imei);
        //更新redis中的路由关系（将连接状态更新为离线）
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String,String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType+":"+imei);
        if(!StringUtils.isBlank(sessionStr)){
            UserSession userSession = JSON.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType+":"+imei, JSONObject.toJSONString(userSession));
        }
        channel.close();
    }
}
