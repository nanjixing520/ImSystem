package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * ClassName: ImportGroupMemberReq
 * Package: com.lld.im.service.group.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/22 下午7:07
 * Version 1.0
 */
@Data
public class ImportGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private List<GroupMemberDto> members;

}
