package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: UpdateGroupMemberReq
 * Package: com.lld.im.service.group.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/23 下午4:35
 * Version 1.0
 */
@Data
public class UpdateGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    private String alias;

    private Integer role;

    private String extra;

}