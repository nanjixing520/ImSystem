package com.lld.im.service.group.model.resp;

import com.lld.im.service.group.model.req.GroupMemberDto;
import lombok.Data;

import java.util.List;

/**
 * ClassName: GetGroupResp
 * Package: com.lld.im.service.group.model.resp
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午7:03
 * Version 1.0
 */
@Data
public class GetGroupResp {

    private String groupId;

    private Integer appId;

    //群主id
    private String ownerId;

    //群类型 1私有群（类似微信） 2公开群(类似qq）
    private Integer groupType;

    private String groupName;

    private Integer mute;// 是否全员禁言，0 不禁言；1 全员禁言。

    //    申请加群选项包括如下几种：
//    0 表示禁止任何人申请加入
//    1 表示需要群主或管理员审批
//    2 表示允许无需审批自由加入群组
    private Integer applyJoinType;

    private Integer privateChat; //是否禁止私聊，0 允许群成员发起私聊；1 不允许群成员发起私聊。

    private String introduction;//群简介

    private String notification;//群公告

    private String photo;//群头像

    private Integer maxMemberCount;//群成员上限

    private Integer status;//群状态 0正常 1解散

    private List<GroupMemberDto> memberList;
}

