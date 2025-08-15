package com.lld.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;


/**
 * ClassName: MqMessageProducer
 * Package: com.lld.im.tcp.publish
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/31 上午10:44
 * Version 1.0
 */
@Slf4j
public class MqMessageProducer {
    public static void sendMessage(Message message, Integer command){
        Channel channel=null;
        String channelName= Constants.RabbitConstants.Im2MessageService;
        if(command.toString().startsWith("2")){
            channelName= Constants.RabbitConstants.Im2GroupService;
        }
        try{
            channel= MqFactory.getChannel(channelName);
            JSONObject msg = (JSONObject) JSON.toJSON(message.getMessagePack());
            msg.put("command",command);
            msg.put("imei",message.getMessageHeader().getImei());
            msg.put("clientType",message.getMessageHeader().getClientType());
            msg.put("appId",message.getMessageHeader().getAppId());
            channel.basicPublish(channelName,"",null, msg.toJSONString().getBytes());
        }catch (Exception e){
           log.error("发送消息出现异常：{}",e.getMessage());
        }

    }
}
