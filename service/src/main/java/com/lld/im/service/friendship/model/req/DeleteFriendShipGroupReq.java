package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * ClassName: DeleteFriendShipGroupReq
 * Package: com.lld.im.service.friendship.model.req
 * Description:
 *    删除组同时删除分组下的成员
 * @Author 南极星
 * @Create 2025/7/22 下午2:49
 * Version 1.0
 */
@Data
public class DeleteFriendShipGroupReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotEmpty(message = "分组名称不能为空")
    private List<String> groupName;

}

