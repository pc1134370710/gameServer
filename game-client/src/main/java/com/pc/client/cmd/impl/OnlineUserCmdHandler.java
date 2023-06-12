package com.pc.client.cmd.impl;

import com.pc.client.cache.LocalGameInfo;
import com.pc.client.cmd.CmdHandler;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomPanel;
import com.pc.common.msg.Msg;

/**
 * @description: 处理 获取在线用户数 消息
 * @author: pangcheng
 * @time: 2023/6/12 14:24
 */
public class OnlineUserCmdHandler  implements CmdHandler {
    @Override
    public void doHandle(Msg msg, GamePanel gamePanel, RoomPanel roomPanel) {
        LocalGameInfo.roomJFrame.setTitle("当前玩家：" + LocalGameInfo.userId+", 当前在线用户数："+msg.getData());
        LocalGameInfo.gameFrame.setTitle("当前玩家：" + LocalGameInfo.userId+", 当前在线用户数："+msg.getData());
    }
}
