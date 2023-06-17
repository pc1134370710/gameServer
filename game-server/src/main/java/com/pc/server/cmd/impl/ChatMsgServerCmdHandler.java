package com.pc.server.cmd.impl;

import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.RoomServer;
import com.pc.server.RpcNettyServer;
import io.netty.channel.Channel;

/**
 * @description: 聊天消息转发
 * @author: pangcheng
 * @create: 2023-06-17 13:52
 **/
public class ChatMsgServerCmdHandler  implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {
        System.out.println(msg);
        RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
        roomServer.putChatMsg(msg);
    }
}
