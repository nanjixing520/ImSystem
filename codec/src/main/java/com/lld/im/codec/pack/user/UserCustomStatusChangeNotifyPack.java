package com.lld.im.codec.pack.user;

import lombok.Data;

/**
 * ClassName: UserCustomStatusChangeNotifyPack
 * Package: com.lld.im.codec.pack.user
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/23 上午12:34
 * Version 1.0
 */
@Data
public class UserCustomStatusChangeNotifyPack {

    private String customText;

    private Integer customStatus;

    private String userId;

}

