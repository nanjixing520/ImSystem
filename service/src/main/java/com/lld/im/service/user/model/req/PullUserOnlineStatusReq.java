package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * ClassName: PullUserOnlineStatusReq
 * Package: com.lld.im.service.user.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/23 上午1:35
 * Version 1.0
 */
@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
