package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.client.model.UserRoleModel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;

import java.util.Map;

/**
 * @description:  开始游戏
 * @author: pangcheng
 * @time: 2023/6/12 13:46
 */
public class StartGameCmdHandler implements CmdHandler {
    @Override
    public void doHandle(Msg msg) {
        Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        UserRoleModel userRoleModel = userRoleModelMap.getOrDefault(userRoleMoveMsgData.getUserId(),new UserRoleModel());
        userRoleModel.analysisMsg(userRoleMoveMsgData);
        LocalGameInfo.gamePanel.addUser(userRoleModel);
    }
}
