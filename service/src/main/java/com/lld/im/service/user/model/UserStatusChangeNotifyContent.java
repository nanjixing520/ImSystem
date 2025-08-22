package com.lld.im.service.user.model;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * ClassName: UserStatusChangeNotifyContent
 * Package: com.lld.im.service.user.model
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 下午8:52
 * Version 1.0
 */
@Data
public class UserStatusChangeNotifyContent extends ClientInfo {


    private String userId;

    //服务端状态 1上线 2离线
    private Integer status;



}

