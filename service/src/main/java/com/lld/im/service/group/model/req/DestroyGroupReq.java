package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * ClassName: DestroyGroupReq
 * Package: com.lld.im.service.group.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/23 上午11:25
 * Version 1.0
 */
@Data
public class DestroyGroupReq extends RequestBase {
    @NotNull(message = "群id不能为空")
    private String groupId;

    private String ownerId;
}
