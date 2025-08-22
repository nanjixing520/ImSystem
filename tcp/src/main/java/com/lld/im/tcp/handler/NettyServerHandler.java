package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.LoginPack;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.user.LoginAckPack;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.model.message.CheckSendMessageReq;
import com.lld.im.tcp.feign.FeignMessageService;
import com.lld.im.tcp.publish.MqMessageProducer;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
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
    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId,String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
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
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType()+":"+msg.getMessageHeader().getImei(),JSONObject.toJSONString(userSession));
            //将channel存起来
            SessionSocketHolder.put(msg.getMessageHeader().getAppId(),loginPack.getUserId(),msg.getMessageHeader().getClientType(),msg.getMessageHeader().getImei(),(NioSocketChannel) ctx.channel());
            //将登录消息发送给所有的服务器，处理下线问题 [使用广播模式来踢掉其他冲突的端]
            UserClientDto userClientDto = new UserClientDto();
            userClientDto.setClientType(msg.getMessageHeader().getClientType());
            userClientDto.setUserId(loginPack.getUserId());
            userClientDto.setAppId(msg.getMessageHeader().getAppId());
            userClientDto.setImei(msg.getMessageHeader().getImei());
            //使用Redis的发布订阅模式
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(userClientDto));
            //将状态变更通知给逻辑层
            UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
            userStatusChangeNotifyPack.setAppId(msg.getMessageHeader().getAppId());
            userStatusChangeNotifyPack.setUserId(loginPack.getUserId());
            userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            MqMessageProducer.sendMessage(userStatusChangeNotifyPack,msg.getMessageHeader(), UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());
            //回复登录成功的ack给登录方
            MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
            LoginAckPack loginAckPack = new LoginAckPack();
            loginAckPack.setUserId(loginPack.getUserId());
            loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
            loginSuccess.setData(loginAckPack);
            loginSuccess.setImei(msg.getMessageHeader().getImei());
            loginSuccess.setAppId(msg.getMessageHeader().getAppId());
            ctx.channel().writeAndFlush(loginSuccess);
        }else if(command== SystemCommand.LOGOUT.getCommand()){
            //删除session（内存中的channel）
            //删除redis中的路由关系
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        }else if(command== SystemCommand.PING.getCommand()){
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }else if(command== MessageCommand.MSG_P2P.getCommand()
        ||command== GroupEventCommand.MSG_GROUP.getCommand()){
            try {
                //调用校验消息发送方的接口
                //如果成功投递到mq
                //如果失败直接返回失败的ack
                String toId = "";
                ResponseVO responseVO = new ResponseVO();
                CheckSendMessageReq checkSendMessageReq = new CheckSendMessageReq();
                checkSendMessageReq.setCommand(command);
                checkSendMessageReq.setAppId(msg.getMessageHeader().getAppId());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                checkSendMessageReq.setFromId(fromId);
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    toId = jsonObject.getString("toId");
                    checkSendMessageReq.setToId(toId);
                    //向逻辑层私聊消息前置校验的方法发送请求
                    responseVO = feignMessageService.checkSendMessage(checkSendMessageReq);
                }else {
                    toId = jsonObject.getString("groupId");
                    checkSendMessageReq.setToId(toId);
                    //向逻辑层群聊消息前置校验的方法发送请求
                    responseVO = feignMessageService.checkSendGroupMessage(checkSendMessageReq);
                }
                //如果校验成功
                if(responseVO.isOk()){
                    MqMessageProducer.sendMessage(msg,command);
                }else{
                    Integer ackCommand = 0;
                    if(command == MessageCommand.MSG_P2P.getCommand()){
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    }else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }
                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setCommand(ackCommand);
                    ack.setData(responseVO);
                    logger.info("tcp message ack failed");
                    ctx.channel().writeAndFlush(ack);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            MqMessageProducer.sendMessage(msg,command);
        }
    }

    /**
     * channelInactive是Netty中ChannelInboundHandler接口的回调方法，
     * 当客户端与服务端的连接关闭时（通道变为非活跃状态）会自动触发。
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        logger.info("Channel inactive, setting user session to offline.");
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
//        ctx.close();（上述方法中已经关闭过通道了，无需重复操作！！）
    }
//该类专注写业务逻辑，在HeartBeatHandler处理器中处理读写超时事件
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
//    }
}