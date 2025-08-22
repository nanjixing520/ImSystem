package com.lld.im.service.utils;


/**
 * @author: 南极星
 **/
public class ConversationIdGenerate {

    //A toB Bto A结果都是一样的
    public static String generateP2PId(String fromId,String toId){
        int i = fromId.compareTo(toId);
        if(i < 0){
            return toId+"|"+fromId;
        }else if(i > 0){
            return fromId+"|"+toId;
        }

        throw new RuntimeException("");
    }
}
