package com.lld.im.tcp.receiver.process;

import com.lld.im.codec.proto.MessagePack;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * ClassName: BaseProcess
 * Package: com.lld.im.tcp.receiver.process
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/11 上午10:33
 * Version 1.0
 */
public abstract class BaseProcess {
    public abstract void processBefore();

    public void process(MessagePack messagePack){
        processBefore();
        NioSocketChannel channel = SessionSocketHolder.get(messagePack.getAppId(),
                messagePack.getToId(), messagePack.getClientType(),
                messagePack.getImei());
        if(channel != null){
            channel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();
}
