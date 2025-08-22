package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lld.im.codec.pack.message.MessageReadedPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.common.model.message.*;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    @Autowired
    RedisTemplate redisTemplate;

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

    /**
     * 同步离线消息(存储在redis中)
     *  基于 ZSet 的分数（序列号）实现
     *  score：messageKey 消息主键是根据雪花算法生成的，可以体现消息产生的先后顺序，所以作为序列号
     * @param req
     * @return
     */
    public ResponseVO syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();
        //获取离线消息最大的seq（实际是消息主键）
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //将消息队列倒序查询第一个元素（默认是升序，倒序就是降序），查询的就是最新的消息序列号，也就是最大的序列号
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if(!CollectionUtils.isEmpty(set)){
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }
        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);
        //根据score范围正序查询 ZSet 中的元素 序列号在 [lastSequence, maxSeq] 范围内
        //最后两个参数依次是偏移量（从筛选结果的第 offset 条开始取），提取数量（从 offset 开始，最多取 count 条）
        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if(!CollectionUtils.isEmpty(respList)){
            //获取本次拉取到的离线消息中最新的消息
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }
}
