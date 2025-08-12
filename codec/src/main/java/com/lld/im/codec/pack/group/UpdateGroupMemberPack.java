package com.lld.im.codec.pack.group;

import lombok.Data;

/**
 * @author: 南极星
 * @description: 修改群成员通知报文
 **/
@Data
public class UpdateGroupMemberPack {

    private String groupId;

    private String memberId;

    private String alias;

    private String extra;
}
