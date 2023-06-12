package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.client.model.UserRoleModel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.UserRoleMsgData;

import java.util.Map;

/**
 * @description: 玩家 释放闪现
 * @author: pangcheng
 * @time: 2023/6/12 14:17
 */
public class UserFlashCmdHandler implements CmdHandler {
    /**
     * 释放闪现 也只是修改 X 坐标轴， 其余不变
     * @param msg 消息对象
     */
    @Override
    public void doHandle(Msg msg) {
        Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
        UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
        UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
//        userRoleModel.analysisMoveMsg(userRoleMoveMsgData);
        userRoleModel.setUserX(userRoleMoveMsgData.getUserX());
    }
}
