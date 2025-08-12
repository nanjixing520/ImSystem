package com.lld.im.tcp.receiver.process;

/**
 * ClassName: ProcessFactory
 * Package: com.lld.im.tcp.receiver.process
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/11 上午10:33
 * Version 1.0
 */
public class ProcessFactory {
    private static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess(Integer command) {
        return defaultProcess;
    }
}
