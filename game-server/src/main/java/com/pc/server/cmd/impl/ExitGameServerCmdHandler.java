package com.pc.server.cmd.impl;

import com.pc.common.netty.cache.RoomCache;
import com.pc.common.netty.model.UserModel;
import com.pc.common.prtotcol.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.netty.model.RoomServer;

/**
 * @description: 退出游戏
 * @author: pangcheng
 * @time: 2023/6/12 16:03
 */
public class ExitGameServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, UserModel userModel) {
        RoomServer roomServer = RoomCache.get(msg.getRoomId());
        // 移除房间内的用户
        roomServer.getUser().invalidate(msg.getUserId());
        roomServer.getTaskChannel().invalidate(msg.getUserId());
        roomServer.getChatChannel().invalidate(msg.getUserId());

        // 告诉所有玩家 这个人走了， 移除改用户
        Msg msg1 = Msg.getMsg(ServerCmd.EXIT_GAME.getValue(), msg.getUserId(), null);
        roomServer.putMsg(msg1);
    }

}
