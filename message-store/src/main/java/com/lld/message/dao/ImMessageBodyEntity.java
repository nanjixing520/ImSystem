package com.lld.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * ClassName: ImMessageBodyEntity
 * Package: com.lld.message.dao.mapper
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/18 上午10:44
 * Version 1.0
 */
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;

    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}