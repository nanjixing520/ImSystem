package com.lld.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;

/**
 * ClassName: MessageProducer
 * Package: com.lld.im.tcp.utils
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/6 下午8:50
 * Version 1.0
 */
@Component
public class MessageProducer {
    private static Logger logger= LoggerFactory.getLogger(MessageProducer.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UserSessionUtils userSessionUtils;
    private String exchangeName = Constants.RabbitConstants.MessageService2Im;

    //最底层发消息方法sendMessage
     public boolean sendMessage(UserSession session, Object msg) {
         try{
             logger.info("send message =="+msg);
             /**
              * 消息先发送到 exchangeName 指定的交换机；
              * 交换机根据 session.getBrokerId() 这个路由键，找到所有绑定了相同路由键的队列；
              * 最终将 msg 消息内容存入匹配的队列中，等待消费者（如 MessageReceiver）处理。
              */
             rabbitTemplate.convertAndSend(exchangeName,session.getBrokerId()+"",msg);//交换机名称,路由键,消息
             return true;
         }catch (Exception e){
             logger.error("send error :"+e.getMessage());
             return false;
         }
     }
     //封装真正的数据包，调用sendMessage，在sendPack中把包装好的数据发送给sendMessage
     public boolean sendPack(String toId, Command command,Object msg,UserSession userSession) {
         //数据包给tcp服务看
         MessagePack messagePack = new MessagePack();
         messagePack.setToId(toId);//toId指定发送给谁
         messagePack.setCommand(command.getCommand());//根据command去指定不同的发消息策略
         messagePack.setClientType(userSession.getClientType());
         messagePack.setAppId(userSession.getAppId());
         messagePack.setUserId(userSession.getUserId());
         messagePack.setImei(userSession.getImei());//这几个属性指定channel
         JSONObject jsonObject=JSONObject.parseObject(JSONObject.toJSONString(msg));
         messagePack.setData(jsonObject);//真正发送的消息数据是msg，将其转化成JsonObject，作为数据包中的data 其他的都是路由属性
         String body = JSONObject.toJSONString(messagePack);
         return sendMessage(userSession,body);
     }
    //发送给某一用户的所有端
    public void sendToUser(String toId, Command command, Object data, Integer appId) {
        List<UserSession> userSession
                = userSessionUtils.getUserSession(appId, toId);
        for (UserSession session : userSession) {
            sendPack(toId, command, data, session);
        }
    }
   //重载方法（如果客户端类型和设备号为空，那么发送给用户的所有端，否则发送给除此设备以外的其他所有设备）
    public void sendToUser(String toId, Integer clientType,String imei, Command command,
                           Object data, Integer appId){
        if(clientType != null && StringUtils.isNotBlank(imei)){
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId,command,data,clientInfo);
        }else{
            sendToUser(toId,command,data,appId);
        }
    }

    //发送给某个用户的指定客户端
    public void sendToUser(String toId, Command command
            , Object data, ClientInfo clientInfo){
        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(),
                clientInfo.getImei());
        sendPack(toId,command,data,userSession);
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

    //发送给除了某一端的其他端
    public void sendToUserExceptClient(String toId, Command command
            , Object data, ClientInfo clientInfo){
        List<UserSession> userSession = userSessionUtils
                .getUserSession(clientInfo.getAppId(),
                        toId);
        for (UserSession session : userSession) {
            if(!isMatch(session,clientInfo)){
                sendPack(toId,command,data,session);
            }
        }
    }


}
