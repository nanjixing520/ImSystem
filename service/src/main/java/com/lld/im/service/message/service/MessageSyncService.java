package com.lld.im.service.message.service;

import com.lld.im.codec.pack.message.MessageReadedPack;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.message.GroupChatMessageContent;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.MessageReadedContent;
import com.lld.im.common.model.message.MessageReciveAckContent;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: MessageSyncService
 * Package: com.lld.im.service.message.service
 * Description:
 *   消息同步处理类
 * @Author 南极星
 * @Create 2025/8/18 下午6:47
 * Version 1.0
 */
@Service
public class MessageSyncService {
    @Autowired
    MessageProducer messageProducer;
    @Autowired
    ConversationService conversationService;

    /**
     * 逻辑层将消息接收端返回的消息接受ack转发给消息发送端的所有客户端
     * (按照我的理解应该是发送给发送端的那一个发起端，但是这样也行，以老师为主，服务端代发ack的时候是发送给某一端的)
     * @param messageContent
     */
    public void receiveMark(MessageReciveAckContent messageContent) {
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_RECIVE_ACK,
                messageContent,messageContent.getAppId());
    }
    /**
     * @description: 处理接收端发来的私聊消息已读
     * 更新会话的seq，通知在线的同步端发送指定command ，发送已读回执通知对方（消息发起方）我已读
     * @param
     * @return void
     * @author lld
     */
    public void readMark(MessageReadedContent messageContent) {
        conversationService.messageMarkRead(messageContent);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent,messageReadedPack);
        //同步自己的其他端消息已读
        syncToSender(messageReadedPack,messageContent,MessageCommand.MSG_READED_NOTIFY);
        //通知消息发起方已读
        messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_READED_RECEIPT,messageReadedPack,messageContent.getAppId());
    }

    /**
     * 更新私聊消息发送方会话的seq
     * @param messageContent
     */
    public void fromedReadMark(MessageContent messageContent) {
        MessageReadedContent messageReadedContent = new MessageReadedContent();
        BeanUtils.copyProperties(messageContent,messageReadedContent);
        messageReadedContent.setConversationType( ConversationTypeEnum.P2P.getCode());
        conversationService.messageMarkRead(messageReadedContent);
    }
    /**
     * 更新群聊消息发送方会话的seq
     * @param messageContent
     */
    public void fromedGroupReadMark(GroupChatMessageContent messageContent) {
        MessageReadedContent messageReadedContent = new MessageReadedContent();
        BeanUtils.copyProperties(messageContent,messageReadedContent);
        messageReadedContent.setConversationType( ConversationTypeEnum.GROUP.getCode());
        conversationService.messageMarkRead(messageReadedContent);
    }

    /**
     * 同步自己的其他端消息已读
     * @param pack  数据包
     * @param content 当前客户端类型
     * @param command
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command){
        messageProducer.sendToUserExceptClient(pack.getFromId(),
                command,pack,
                content);
    }

    /**
     * 处理接收端发来的群聊消息已读
     * @param messageReaded
     */
    public void groupReadMark(MessageReadedContent messageReaded) {
        conversationService.messageMarkRead(messageReaded);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded,messageReadedPack);
        syncToSender(messageReadedPack,messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY
        );
        messageProducer.sendToUser(messageReadedPack.getToId(),GroupEventCommand.MSG_GROUP_READED_RECEIPT
                    ,messageReaded,messageReaded.getAppId());

    }
}
