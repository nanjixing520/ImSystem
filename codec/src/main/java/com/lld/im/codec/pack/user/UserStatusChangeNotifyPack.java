package com.lld.im.codec.pack.user;

import com.lld.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * ClassName: UserStatusChangeNotifyPack
 * Package: com.lld.im.codec.pack.user
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/22 下午7:55
 * Version 1.0
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}

