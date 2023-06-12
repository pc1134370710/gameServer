package com.pc.client.cmd;

import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomPanel;
import com.pc.common.msg.Msg;

/**
 * @description: 玩家相关命令字处理器
 * @author: pangcheng
 * @time: 2023/6/12 13:41
 */
public interface CmdHandler {

    /**
     *  处理动作
     * @param msg 消息对象
     * @param gamePanel 游戏面板
     * @param roomPanel 房间面板
     */
    void doHandle(Msg msg, GamePanel gamePanel, RoomPanel roomPanel);
}
