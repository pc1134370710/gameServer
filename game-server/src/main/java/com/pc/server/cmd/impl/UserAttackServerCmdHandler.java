package com.pc.server.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;
import com.pc.server.RoomServer;
import com.pc.server.RpcNettyServer;
import com.pc.server.UserModel;
import io.netty.channel.Channel;

/**
 * @description: 处理用户普通攻击
 * @author: pangcheng
 * @time: 2023/6/12 16:31
 */
public class UserAttackServerCmdHandler  implements ServerCmdHandler {

    /**
     * 普通攻击 只接受  attack 消息
     * @param msg 消息对象
     * @param channel
     */
    @Override
    public void doHandle(Msg msg, Channel channel) {
        RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
        // 存储技能
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        // 设置该用户的 攻击状态
        UserModel userModel = roomServer.getUser().get(userRoleMoveMsgData.getUserId());
        userModel.analysisAttackMsg(userRoleMoveMsgData);
        if(userRoleMoveMsgData.getAttack()){
            // 如果是触发攻击， 房间会检测攻击是否生效
            try {
                roomServer.getAttackQueue().put(userModel.getUserRoleMsgData());
            } catch (InterruptedException e) {
            }
        }
        // 通知其他人，有人释放技能了
        roomServer.putMsg(msg);
    }
}
