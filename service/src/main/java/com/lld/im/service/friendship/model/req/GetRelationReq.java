package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: GetRelationReq
 * Package: com.lld.im.service.friendship.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/21 下午7:15
 * Version 1.0
 */
@Data
public class GetRelationReq extends RequestBase {
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotBlank(message = "toId不能为空")
    private String toId;
}
