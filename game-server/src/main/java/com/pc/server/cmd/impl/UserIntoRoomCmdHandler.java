package com.pc.server.cmd.impl;

import com.pc.common.cmd.CmdHandler;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.RoomServer;
import com.pc.server.RpcNettyServer;
import com.pc.server.UserModel;
import io.netty.channel.Channel;

/**
 * @description: 玩家 进入房间等待
 * @author: pangcheng
 * @time: 2023/6/12 16:08
 */
public class UserIntoRoomCmdHandler implements ServerCmdHandler {
    @Override
    public void doHandle(Msg msg, Channel channel) {
        UserModel userModel = new UserModel();
        userModel.setUserId(msg.getUserId());
        userModel.setChannel(channel);
        userModel.setStart(1);
        // 建立房间跟 通道的关系
        // 将用户加入到房间中
        RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
        roomServer.getUser().put(userModel.getUserId(), userModel);

        // 刷新房间数据, todo 通知房间内的用户，当前进来多少人了
    }
}
