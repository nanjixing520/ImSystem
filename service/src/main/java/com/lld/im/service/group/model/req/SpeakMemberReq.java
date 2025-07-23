package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * ClassName: SpeaMemberReq
 * Package: com.lld.im.service.group.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/23 下午4:35
 * Version 1.0
 */
@Data
public class SpeakMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    //禁言时间，单位毫秒
    @NotNull(message = "禁言时间不能为空")
    private Long speakDate;
}
