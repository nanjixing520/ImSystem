package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * ClassName: AddGroupMemberReq
 * Package: com.lld.im.service.group.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午7:01
 * Version 1.0
 */
@Data
public class AddGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotEmpty(message = "群成员不能为空")
    private List<GroupMemberDto> members;
}
