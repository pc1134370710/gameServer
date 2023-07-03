package com.pc.server.cmd.impl;

import com.pc.server.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.cache.RoomCache;
import com.pc.server.model.UserModel;
import com.pc.server.model.RoomServer;

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
