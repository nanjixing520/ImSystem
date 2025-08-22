package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * ClassName: SetUserCustomerStatusReq
 * Package: com.lld.im.service.user.model.req
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/23 上午12:16
 * Version 1.0
 */
@Data
public class SetUserCustomerStatusReq extends RequestBase {

    private String userId;
    //客户端状态名称，就是显示的文本，例如隐身/忙碌
    private String customText;
    //客户端状态
    private Integer customStatus;

}
