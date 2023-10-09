package com.pc.server.cmd.impl;

import com.pc.server.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.cache.RoomCache;
import com.pc.server.model.UserModel;
import com.pc.server.model.RoomServer;
import io.netty.channel.Channel;

/**
 * @description: 注册聊天 管道处理器
 * @author: pangcheng
 * @create: 2023-06-17 13:24
 **/
public class InitChatServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {

        // 建立房间跟 通道的关系
        // 将用户加入到房间中
        RoomServer roomServer = RoomCache.get(msg.getRoomId());
        UserModel userModel =  roomServer.getUser().get(msg.getUserId());
        userModel.setChatChannel(channel);
    }

}
