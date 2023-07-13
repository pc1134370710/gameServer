package com.pc.server.cmd.impl;

import com.pc.server.RpcNettyServer;
import com.pc.server.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.cache.RoomCache;
import com.pc.server.cache.UserCache;
import com.pc.server.model.RoomServer;
import com.pc.server.model.UserModel;
import io.netty.channel.Channel;

/**
 * @description: 玩家 进入房间等待
 * @author: pangcheng
 * @time: 2023/6/12 16:08
 */
public class UserIntoRoomCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {
        RoomServer roomServer = RoomCache.get(msg.getRoomId());
        UserModel userModel =  new UserModel();
        userModel.setUserId(msg.getUserId());
        userModel.setStart(1);
        userModel.setChannel(channel);
        // 建立房间跟 通道的关系
        // 将用户加入到房间中
        roomServer.getUser().put(userModel.getUserId(), userModel);
        // 刷新房间数据，TODO 通知房间内的用户，当前进来多少人了
    }

}
