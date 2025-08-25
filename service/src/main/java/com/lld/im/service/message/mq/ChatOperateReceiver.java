package com.lld.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.MessageReadedContent;
import com.lld.im.common.model.message.MessageReciveAckContent;
import com.lld.im.common.model.message.RecallMessageContent;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName: ChatOperateReceiver
 * Package: com.lld.im.service.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/12 上午11:18
 * Version 1.0
 */
@Component
public class ChatOperateReceiver {
    @Autowired
    private P2PMessageService p2PMessageService;
    @Autowired
    private MessageSyncService messageSyncService;
    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);
    /**
     * 处理从消息队列接收到的聊天消息
     *
     * @param message 消息队列中的消息对象，包含消息体等信息
     * @param headers 消息的头信息，包含交换机、路由键、投递标签等元数据
     * @param channel 与RabbitMQ通信的信道对象，用于消息确认等操作
     * @throws Exception 处理过程中可能抛出的异常
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2MessageService,durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService,durable = "true")
                    // 未指定 routingKey，默认为空字符串
            ),concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String,Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(),"utf-8");
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);
        // 从消息头中获取投递标签（delivery tag），用于消息确认
        // 投递标签是RabbitMQ为每个消息分配的唯一标识，用于区分不同消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try{
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if(command.equals(MessageCommand.MSG_P2P.getCommand())){
                //处理私聊消息
                MessageContent messageContent
                        = jsonObject.toJavaObject(MessageContent.class);
                p2PMessageService.process(messageContent);
            }else if(command.equals(MessageCommand.MSG_RECIVE_ACK.getCommand())){
                //消息接受方发来的消息接收确认
                MessageReciveAckContent messageContent
                        = jsonObject.toJavaObject(MessageReciveAckContent.class);
                messageSyncService.receiveMark(messageContent);
            }else if(command.equals(MessageCommand.MSG_READED.getCommand())){
                //消息接收确认
                MessageReadedContent messageContent
                        = jsonObject.toJavaObject(MessageReadedContent.class);
                messageSyncService.readMark(messageContent);
            }else if (Objects.equals(command, MessageCommand.MSG_RECALL.getCommand())) {
//                撤回消息
                RecallMessageContent messageContent = JSON.parseObject(msg, new TypeReference<RecallMessageContent>() {
                }.getType());
                messageSyncService.recallMessage(messageContent);
            }
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }
    }
}