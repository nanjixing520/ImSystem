package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
