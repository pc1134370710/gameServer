package com.pc.server.cmd.impl;

import com.pc.common.ServerCmd;
import com.pc.common.cmd.CmdHandler;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.RoomServer;
import com.pc.server.RpcNettyServer;
import io.netty.channel.Channel;

/**
 * @description: 退出游戏
 * @author: pangcheng
 * @time: 2023/6/12 16:03
 */
public class ExitGameServerCmdHandler  implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {
        RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
        // 移除房间内的用户
        roomServer.getUser().remove(msg.getUserId());
        roomServer.getTaskChannel().remove(msg.getUserId());
        roomServer.getChatChannel().remove(msg.getUserId());

        // 告诉所有玩家 这个人走了， 移除改用户
        Msg msg1 = Msg.getMsg(ServerCmd.EXIT_GAME.getValue(), msg.getUserId(), null);
        roomServer.putMsg(msg1);
    }
}
