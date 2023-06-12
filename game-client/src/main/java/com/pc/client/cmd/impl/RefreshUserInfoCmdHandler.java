package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.client.model.UserRoleModel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;

import java.util.Map;

/**
 * @description:  刷新用户游戏中的信息 ，HP, MP 是否死亡等
 * @author: pangcheng
 * @time: 2023/6/12 14:11
 */
public class RefreshUserInfoCmdHandler implements CmdHandler {
    @Override
    public void doHandle(Msg msg) {
        Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
        userRoleModel.analysisMsg(userRoleMoveMsgData);
    }
}
