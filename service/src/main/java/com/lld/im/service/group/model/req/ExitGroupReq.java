package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: ExitGroupReq
 * Package: com.lld.im.service.group.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/23 下午4:27
 * Version 1.0
 */
@Data
public class ExitGroupReq extends RequestBase {
    @NotBlank(message = "groupId不能为空")
    private String groupId;
}
