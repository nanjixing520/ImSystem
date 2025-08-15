package com.lld.im.service.message.service;

import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: P2PMessageService
 * Package: com.lld.im.service.message.service
 * Description:
 *   消息的处理
 * @Author 南极星
 * @Create 2025/8/14 上午10:38
 * Version 1.0
 */
@Service
public class P2PMessageService {

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;
    @Autowired
    MessageStoreService messageStoreService;

    public void process(MessageContent messageContent){

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友/拉黑（配置文件中定义开关）
        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, messageContent);
        if(responseVO.isOk()){
            //回包之前将数据持久化
            messageStoreService.storeP2PMessage(messageContent);
            //1.回ack成功给自己
            ack(messageContent, responseVO);
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方在线端
            dispatchMessage(messageContent);
        }else{
            //告诉客户端失败了
            //回ack失败给自己
            ack(messageContent, responseVO);
        }
    }

    //分发消息给接收端的所有端
    private void dispatchMessage(MessageContent messageContent){
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P,
                messageContent, messageContent.getAppId());
    }

    //返回ack信息
    private void ack(MessageContent messageContent,ResponseVO responseVO){
        logger.info("msg ack,msgId={},checkResult{}",messageContent.getMessageId(),responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                responseVO, messageContent
        );
    }

    //发送消息给其他端（同步消息给发送端的其他客户端）
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);
    }


    //前置校验
    private ResponseVO imServerPermissionCheck(String fromId, String toId,
                                               MessageContent messageContent){
        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, messageContent.getAppId());
        if(!responseVO.isOk()){
            return responseVO;
        }
        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, messageContent.getAppId());
        return responseVO;
    }

    public SendMessageResp send(SendMessageReq req) {
        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req,message);
        //1.数据持久化（并且实现了对消息唯一标识MessageKey进行赋值）
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        //供后台调用，不需要返回ack
        //2.发消息给同步在线端
        syncToSender(message,message);
        //3.发消息给对方在线端
        dispatchMessage(message);
        return sendMessageResp;
    }
}
