package com.lld.im.common.enums.command;


/**
 * ClassName: SystemCommandEnum
 * Package: com.lld.im.common.enums
 * Description:
 *
 * @Author 南极星
 * @Create 2025/7/29 上午11:15
 * Version 1.0
 */
public enum SystemCommand implements Command {
    /**
     * 指令使用十六进制
     * 登录9000
     */
    LOGIN(0x2328),
    //登出  9003
    LOGOUT(0x232b),
    //心跳 9999
    PING(0x270f),
    //下线通知 用于多端互斥  9002
    MUTUALLOGIN(0x232a);
    private int command;
    SystemCommand(int command) {
        this.command = command;
    }
    @Override
    public int getCommand() {
        return this.command;
    }
}
