package com.pc.client.cmd.impl;

import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.common.msg.Msg;

/**
 * @description: 玩家退出游戏 消息处理器
 * @author: pangcheng
 * @time: 2023/6/12 14:24
 */
public class ExitGameCmdHandler  implements CmdHandler {
    @Override
    public void doHandle(Msg msg) {
        // 移除该用户
        LocalGameInfo.userRoleModelMap.remove(msg.getUserId());
    }
}
