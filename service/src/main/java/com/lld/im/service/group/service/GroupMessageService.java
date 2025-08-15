package com.lld.im.service.group.service;

import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.GroupChatMessageContent;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.service.group.model.req.SendGroupMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.message.service.CheckSendMessageService;
import com.lld.im.service.message.service.MessageStoreService;
import com.lld.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: GroupMessageService
 * Package: com.lld.im.service.group.service
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/14 下午8:09
 * Version 1.0
 */
@Service
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;
    @Autowired
    MessageStoreService messageStoreService;

    public void process(GroupChatMessageContent messageContent){

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
        //群的前置校验
        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId,
                appId);
        if(responseVO.isOk()){
            //回包之前将数据持久化
            messageStoreService.storeGroupMessage(messageContent);
            //1.回ack成功给自己
            ack(messageContent, responseVO);
            //2.发消息给同步在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给除了自己的群成员的在线端
            dispatchMessage(messageContent);
        }else{
            //告诉客户端失败了
            //回ack失败给自己
            ack(messageContent, responseVO);
        }
    }

    //分发消息给发送端
    private void dispatchMessage(GroupChatMessageContent messageContent){

        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                messageContent.getAppId());
        for (String memberId : groupMemberId) {
            if(!memberId.equals(messageContent.getFromId())){
                messageProducer.sendToUser(memberId,
                        GroupEventCommand.MSG_GROUP,
                        messageContent, messageContent.getAppId());
            }
        }
    }

    //返回ack信息
    private void ack(MessageContent messageContent, ResponseVO responseVO){

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(),
                GroupEventCommand.GROUP_MSG_ACK,
                responseVO, messageContent
        );
    }

    //发送消息给其他端
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                GroupEventCommand.MSG_GROUP,messageContent,messageContent);
    }


    //群的前置校验
    private ResponseVO imServerPermissionCheck(String fromId, String groupId, Integer appId){
        ResponseVO responseVO = checkSendMessageService
                .checkGroupMessage(fromId, groupId, appId);
        return responseVO;
    }
    public SendMessageResp send(SendGroupMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req,message);
        //1.数据持久化（并且实现了对消息唯一标识MessageKey进行赋值）
        messageStoreService.storeGroupMessage(message);
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

