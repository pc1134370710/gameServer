package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.client.model.UserRoleModel;
import com.pc.common.cmd.CmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;

import java.util.Map;

/**
 * @description:  初始化npc 电脑玩家
 * @author: pangcheng
 * @time: 2023/6/13 13:51
 */
public class InitNpcUserCmdHandler implements CmdHandler {

    @Override
    public void doHandle(Msg msg) {
        Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        UserRoleModel userRoleModel = userRoleModelMap.getOrDefault(userRoleMoveMsgData.getUserId(),new UserRoleModel());
        userRoleModel.analysisMsg(userRoleMoveMsgData);
        LocalGameInfo.gamePanel.addUser(userRoleModel);
    }
}
