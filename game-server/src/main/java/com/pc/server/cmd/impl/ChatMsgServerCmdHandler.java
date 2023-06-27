package com.pc.server.cmd.impl;

import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.netty.cache.RoomCache;
import com.pc.common.netty.model.UserModel;
import com.pc.common.netty.model.RoomServer;

/**
 * @description: 聊天消息转发
 * @author: pangcheng
 * @create: 2023-06-17 13:52
 **/
public class ChatMsgServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, UserModel userModel) {
        RoomServer roomServer = RoomCache.get(msg.getRoomId());
        roomServer.putChatMsg(msg);
    }

}
