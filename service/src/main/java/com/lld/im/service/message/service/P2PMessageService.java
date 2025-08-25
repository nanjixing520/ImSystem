package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.message.MessageReciveServerAckPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.message.MessageContent;
import com.lld.im.common.model.message.OfflineMessageContent;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.seq.RedisSeq;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.ConversationIdGenerate;
import com.lld.im.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Autowired
    RedisSeq redisSeq;
    @Autowired
    MessageSyncService messageSyncService;
    @Autowired
    AppConfig appConfig;
    @Autowired
    CallbackService callbackService;
    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(MessageContent messageContent){

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //根据messageId从缓存中获取消息
        MessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache
                (appId, messageContent.getMessageId(),MessageContent.class);
        //如果缓存中有消息，说明之前处理过了，只对其进行消息转发，不进行数据持久化
        if (messageFromMessageIdCache != null){
            threadPoolExecutor.execute(() ->{
                //1.回ack成功给自己
                ack(messageFromMessageIdCache,ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageFromMessageIdCache,messageFromMessageIdCache);
                //3.发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageFromMessageIdCache);
                if(clientInfos.isEmpty()){
                    //发送接收确认给发送方，要带上是服务端发送的标识
                    reciverAck(messageFromMessageIdCache);
                }
            });
            return;
        }
        //发送消息之前回调，以下为该回调所在的位置条件
        //位置在查询缓存之后，如果缓存中有消息，说明该消息之前回调成功，已经入库并且可以转发，那么直接转发即可，不需要再次回调判断
        //位置在获取seq之前，如果回调失败消息无需进行入库及转发操作，如果先获取序列号，那么该序列号无效的被自增1
        //获取设置seq就是为了消息入库之后增量拉取用的，所以如果入库条件不满足，也无需分配序列号
        ResponseVO responseVO=ResponseVO.successResponse();
        if(appConfig.isSendMessageBeforeCallback()){
            responseVO = callbackService.beforeCallback(appId, Constants.CallbackCommand.SendMessageBefore,
                    JSONObject.toJSONString(messageContent));
        }
        //回调失败，返回给客户端失败的ack
        if(!responseVO.isOk()){
            ack(messageContent,responseVO);
            return;
        }


        //消息持久化之前分配序列号
        //序列号格式：appId+seq常量+(from+to)/groupId
        long seq = redisSeq.doGetSeq(appId+":"+ Constants.SeqConstants.Message
                +":"+ ConversationIdGenerate.generateP2PId(fromId,toId));
        messageContent.setMessageSequence(seq);
        //前置校验
        // -->转移在TCP层提前校验，此处只需要返回成功的ack，将ack(messageContent, responseVO);
                            // 改为ack(messageContent, ResponseVO.successResponse());
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友/拉黑（配置文件中定义开关）
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if(responseVO.isOk()){
            threadPoolExecutor.execute(()->{
                //消息持久化之前分配序列号
                //序列号格式：appId+seq常量+(from+to)/groupId
//                long seq = redisSeq.doGetSeq(appId+":"+ Constants.SeqConstants.Message
//                        +":"+ ConversationIdGenerate.generateP2PId(fromId,toId));
//                messageContent.setMessageSequence(seq);
                //回包之前将数据持久化（异步到mq中进行操作）
                messageStoreService.storeP2PMessage(messageContent);
                //插入离线消息
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(messageContent,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
                messageStoreService.storeOfflineMessage(offlineMessageContent);
                //1.回ack成功给自己
                ack(messageContent, ResponseVO.successResponse());
                //2.发消息给同步在线端
                syncToSender(messageContent,messageContent);
                //3.发消息给对方在线端(如果对方不在线，由服务端代发消息确认ack)
                List<ClientInfo> clientInfos = dispatchMessage(messageContent);
                //将消息设置到缓存中去
                messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(),
                        messageContent.getMessageId(),messageContent);
                if(clientInfos.isEmpty()){
                    //发送接收确认给发送方，要带上是服务端发送的标识
                    reciverAck(messageContent);
                }
                //更新发送消息方会话的消息序列号（已读情况）
                messageSyncService.fromedReadMark(messageContent);
                //发送消息之后回调
                if(appConfig.isSendMessageAfterCallback()){
                    callbackService.callback(appId,Constants.CallbackCommand.SendMessageAfter,
                            JSONObject.toJSONString(messageContent));
                }
                logger.info("service message ack success");
            });
//        }else{
//            //告诉客户端失败了
//            //回ack失败给自己
//            ack(messageContent, responseVO);
//        }
    }

    //分发消息给接收端的所有端
    private List<ClientInfo> dispatchMessage(MessageContent messageContent){
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P,
                messageContent, messageContent.getAppId());
        return clientInfos;
    }

    //返回ack信息
    private void ack(MessageContent messageContent,ResponseVO responseVO){
        logger.info("msg ack,msgId={},checkResult{}",messageContent.getMessageId(),responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(),
                messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,
                responseVO, messageContent
        );
    }
    public void reciverAck(MessageContent messageContent){
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        //回包给发送者的真正发送端
        messageProducer.sendToUser(messageContent.getFromId(),MessageCommand.MSG_RECIVE_ACK,
                pack,new ClientInfo(messageContent.getAppId(),messageContent.getClientType()
                        ,messageContent.getImei()));
    }

    //发送消息给其他端（同步消息给发送端的其他客户端）
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);
    }


    //前置校验（在保证消息一致性的处理中，我们将此方法设置为公共调用，实际在im服务层被调用）
    public ResponseVO imServerPermissionCheck(String fromId, String toId,
                                               Integer appId){
        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }
        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
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
