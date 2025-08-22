package com.lld.im.common.enums.command;

/**
 先用十进制确定这些指令
 */
public enum UserEventCommand implements Command {

    //用户修改command 4000
    USER_MODIFY(4000),

    //4001 用户在线状态通知报文  TCP层发送给逻辑层
    USER_ONLINE_STATUS_CHANGE(4001),


    //4004 用户在线状态通知报文  逻辑层发送给客户端(TO好友和对应订阅的用户)
    USER_ONLINE_STATUS_CHANGE_NOTIFY(4004),

    //4005 用户在线状态通知同步报文  逻辑层发送给客户端(TO同步端)
    USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC(4005),


    ;

    private int command;

    UserEventCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
