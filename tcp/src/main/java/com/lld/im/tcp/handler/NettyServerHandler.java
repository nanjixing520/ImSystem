package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.LoginPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * ClassName: NettyServerHandler
 * Package: com.lld.im.tcp.handler
 * Description:
 *   专注做业务
 * @Author 南极星
 * @Create 2025/7/28 下午9:57
 * Version 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger= LoggerFactory.getLogger(NettyServerHandler.class);
    private Integer brokerId;

    public NettyServerHandler(Integer brokerId) {
        this.brokerId = brokerId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Integer command = msg.getMessageHeader().getCommand();
        //登录command
        if(command== SystemCommand.LOGIN.getCommand()){
            //将msg.getMessagePack()返回的对象先序列化为 JSON 字符串，再反序列化为LoginPack类型的对象。
            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());
            //为这个通道关联所需的属性
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(msg.getMessageHeader().getClientType());
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(msg.getMessageHeader().getImei());
           //在Redis中用map存储用户session
            UserSession userSession = new UserSession();
            userSession.setUserId(loginPack.getUserId());
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msg.getMessageHeader().getImei());
            try{
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }
            //初始化完属性之后调用redis客户端去保存（启动的时候就要初始化redis客户端,连接地址密码等用配置文件加载）
            //TODO：存到redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType()+":"+msg.getMessageHeader().getImei(),JSONObject.toJSONString(userSession));
            //将channel存起来
            SessionSocketHolder.put(msg.getMessageHeader().getAppId(),loginPack.getUserId(),msg.getMessageHeader().getClientType(),msg.getMessageHeader().getImei(),(NioSocketChannel) ctx.channel());
            UserClientDto userClientDto = new UserClientDto();
            userClientDto.setClientType(msg.getMessageHeader().getClientType());
            userClientDto.setUserId(loginPack.getUserId());
            userClientDto.setAppId(msg.getMessageHeader().getAppId());
            userClientDto.setImei(msg.getMessageHeader().getImei());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(userClientDto));
        }else if(command== SystemCommand.LOGOUT.getCommand()){
            //删除session（内存中的channel）
            //删除redis中的路由关系
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        }else if(command== SystemCommand.PING.getCommand()){
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }
    }
//该类专注写业务逻辑，在HeartBeatHandler处理器中处理读写超时事件
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
//    }
}