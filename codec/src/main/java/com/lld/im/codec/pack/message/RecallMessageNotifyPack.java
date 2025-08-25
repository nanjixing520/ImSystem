package com.lld.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: RecallMessageNotifyPack
 * Package: com.lld.im.codec.pack.message
 * Description:
 *  撤回消息通知报文
 * @Author 南极星
 * @Create 2025/8/23 上午11:18
 * Version 1.0
 */
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
