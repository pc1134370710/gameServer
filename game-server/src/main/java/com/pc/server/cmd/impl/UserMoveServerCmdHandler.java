package com.pc.server.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.server.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;
import com.pc.server.cache.RoomCache;
import com.pc.server.model.RoomServer;
import com.pc.server.model.UserModel;
import io.netty.channel.Channel;

/**
 * @description: 服务器处理 用户移动
 * @author: pangcheng
 * @time: 2023/6/12 16:19
 */
public class UserMoveServerCmdHandler implements ServerCmdHandler {

    // 收到用户移动 TODO 待修改成 服务器端移动用户

    /**
     * 只接受， 坐标， 方向信息
     *
     * @param msg       消息对象
     * @param channel
     */
    @Override
    public void doHandle(Msg msg,  Channel channel) {
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        RoomServer roomServer = RoomCache.get(msg.getRoomId());
        UserModel userModel =  roomServer.getUser().getIfPresent(msg.getUserId());
        // 解析客户端的 操作数据包
        userModel.analysisMoveMsg(userRoleMoveMsgData);
        // 通知其他客户端移动
        roomServer.putMsg(msg);
    }

}
