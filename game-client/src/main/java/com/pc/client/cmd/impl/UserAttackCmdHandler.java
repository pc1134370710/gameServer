package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.client.cmd.CmdHandler;
import com.pc.client.model.UserRoleModel;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomPanel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;

import java.util.Map;

/**
 * @description: 玩家普通攻击 消息处理
 * @author: pangcheng
 * @time: 2023/6/12 13:59
 */
public class UserAttackCmdHandler implements CmdHandler {

    /**
     * 普通攻击 收到的消息应 只包含  userId 跟 attack
     * @param msg 消息对象
     * @param gamePanel 游戏面板
     * @param roomPanel 房间面板
     */
    @Override
    public void doHandle(Msg msg, GamePanel gamePanel, RoomPanel roomPanel) {
        Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
        // 接收到的 攻击
        userRoleModel.analysisAttackMsg(userRoleMoveMsgData);
    }
}
