package com.lld.im.service.group.service;

import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.GroupChatMessageContent;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.OfflineMessageContent;
import com.lld.im.service.group.model.req.SendGroupMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.message.service.CheckSendMessageService;
import com.lld.im.service.message.service.MessageStoreService;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    MessageSyncService messageSyncService;
    private final ThreadPoolExecutor threadPoolExecutor;
    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-group-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }


    public void process(GroupChatMessageContent messageContent){

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(appId,
                messageContent.getMessageId(), GroupChatMessageContent.class);
        if(messageFromMessageIdCache != null){
            threadPoolExecutor.execute(() ->{
                //1.回ack成功给自己
                ack(messageContent,ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageContent,messageContent);
                //3.发消息给对方在线端
                dispatchMessage(messageContent);
            });
        }
        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.GroupMessage
                + ":" + groupId);
        messageContent.setMessageSequence(seq);
//        //群的前置校验
//        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId,
//                appId);
//        if(responseVO.isOk()){
            //优化：通过线程池实现消息存储和消息转发的核心逻辑
            threadPoolExecutor.execute(()->{
                //回包之前将数据持久化（方法内实现优化：通过mq异步持久化数据）
                messageStoreService.storeGroupMessage(messageContent);
                //插入离线消息
                List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                        messageContent.getAppId());
                messageContent.setMemberId(groupMemberId);

                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(messageContent,offlineMessageContent);
                offlineMessageContent.setToId(messageContent.getGroupId());
                messageStoreService.storeGroupOfflineMessage(offlineMessageContent,groupMemberId);
                //1.回ack成功给自己
                ack(messageContent, ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageContent,messageContent);
                //3.发消息给除了自己的群成员的在线端
                dispatchMessage(messageContent);
                //将消息存到缓存中去
                messageStoreService.setMessageFromMessageIdCache(appId,messageContent.getMessageId(),messageContent);
                //更新发送消息方会话的消息序列号（已读情况）
                messageSyncService.fromedGroupReadMark(messageContent);
            });

//        }else{
//            //告诉客户端失败了
//            //回ack失败给自己
//            ack(messageContent, responseVO);
//        }
    }

    //发消息给除了自己的群成员的在线端
    private void dispatchMessage(GroupChatMessageContent messageContent){
        for (String memberId : messageContent.getMemberId()) {
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
    public ResponseVO imServerPermissionCheck(String fromId, String groupId, Integer appId){
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

