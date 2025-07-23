package com.lld.im.service.group.model.resp;

import lombok.Data;

@Data
public class GetRoleInGroupResp {
//当前这个用户在群内的主键
    private Long groupMemberId;

    private String memberId;

    private Integer role;
//禁言时间
    private Long speakDate;

}