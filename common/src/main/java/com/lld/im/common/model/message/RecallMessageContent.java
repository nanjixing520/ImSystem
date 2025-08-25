package com.lld.im.common.model.message;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * ClassName: RecallMessageContent
 * Package: com.lld.im.common.model.message
 * Description:
 *
 * @Author 南极星
 * @Create 2025/8/23 上午11:15
 * Version 1.0
 */
@Data
public class RecallMessageContent extends ClientInfo {
    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageTime;

    private Long messageSequence;

    private Integer conversationType;

}
