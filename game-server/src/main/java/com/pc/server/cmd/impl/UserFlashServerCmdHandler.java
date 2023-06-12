package com.pc.server.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.common.Constant;
import com.pc.common.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;
import com.pc.server.RoomServer;
import com.pc.server.RpcNettyServer;
import com.pc.server.UserModel;
import io.netty.channel.Channel;

/**
 * @description: 服务器处理用户 闪现消息
 * @author: pangcheng
 * @time: 2023/6/12 16:27
 */
public class UserFlashServerCmdHandler implements ServerCmdHandler {

    /**
     * 只接受 闪现消息
     * @param msg 消息对象
     * @param channel
     */
    @Override
    public void doHandle(Msg msg, Channel channel) {
        RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        UserModel userModel = roomServer.getUser().get(userRoleMoveMsgData.getUserId());
        userModel.analysisSlideMsg(userRoleMoveMsgData);

        // 蓝条归0
        userModel.getUserRoleMsgData().setMp(userModel.getUserRoleMsgData().getMp()-50);
        Msg msg1 = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), userModel.getUserId(), userModel.getUserRoleMsgData());
        roomServer.putMsg(msg1);

        Msg msg2 = Msg.getMsg(ServerCmd.USER_FLASH.getValue(), userModel.getUserId(), userModel.getUserRoleMsgData());
        // 通知其他人，玩家滑行技能
        roomServer.putMsg(msg2);
    }
}
