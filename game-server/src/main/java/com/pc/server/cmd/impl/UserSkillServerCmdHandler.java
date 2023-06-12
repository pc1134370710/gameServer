package com.pc.server.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.common.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;
import com.pc.server.RoomServer;
import com.pc.server.RpcNettyServer;
import com.pc.server.UserModel;
import io.netty.channel.Channel;

/**
 * @description: 服务器 处理释放技能消息
 * @author: pangcheng
 * @time: 2023/6/12 16:21
 */
public class UserSkillServerCmdHandler  implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {
        RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
        UserModel userModel = roomServer.getUser().get(msg.getUserId());
        // 存储技能
        SkillMsgData skillMsgData = JSON.parseObject(msg.getData(),SkillMsgData.class);
        // 设置技能方向坐标
        skillMsgData.setIsLeftMove(userModel.getUserRoleMsgData().getDirection());
        skillMsgData.setX(userModel.getUserRoleMsgData().getUserX());
        skillMsgData.setY(userModel.getUserRoleMsgData().getUserY());

        roomServer.getSkillMsgDataMap().put(skillMsgData.getSkillId(),skillMsgData);
        // 用户mp 归0， 发送刷新消息

        userModel.getUserRoleMsgData().setMp(userModel.getUserRoleMsgData().getMp()-50);
        Msg msg1 = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), userModel.getUserId(), userModel.getUserRoleMsgData());
        roomServer.putMsg(msg1);
        // 通知其他人，有人释放技能了
        msg.setData(JSON.toJSONString(skillMsgData));
        roomServer.putMsg(msg);
    }
}
