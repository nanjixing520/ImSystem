package com.lld.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
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
    public static void sendMessage(Object message){
        Channel channel=null;
        String channelName="";
        try{
            channel= MqFactory.getChannel(channelName);
            channel.basicPublish(channelName,"",null, JSONObject.toJSONString(message).getBytes());
        }catch (Exception e){
           log.error("发送消息出现异常：{}",e.getMessage());
        }

    }
}
