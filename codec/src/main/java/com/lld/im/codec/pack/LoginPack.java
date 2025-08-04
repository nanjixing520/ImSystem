package com.lld.im.codec.pack;

import lombok.Data;

/**
 * ClassName: LoginPack
 * Package: com.lld.im.codec.pack
 * Description:
 *    专门存放服务端和客户端之间登录传输的数据包
 * @Author 南极星
 * @Create 2025/7/30 上午8:37
 * Version 1.0
 */
@Data
public class LoginPack {
    private String userId;
}
