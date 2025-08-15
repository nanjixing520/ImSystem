package com.lld.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * ClassName: ImGroupMessageHistoryEntity
 * Package: com.lld.im.service.group.dao
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/15 上午9:15
 * Version 1.0
 */
@Data
@TableName("im_group_message_history")
public class ImGroupMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String groupId;

    /** messageBodyId*/
    private Long messageKey;
    /** 序列号*/
    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;


}
