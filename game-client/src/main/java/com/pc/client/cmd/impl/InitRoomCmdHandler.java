package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pc.client.cmd.CmdHandler;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomButton;
import com.pc.client.gui.RoomPanel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.RoomMsgData;

import java.util.List;

/**
 * @description: 初始化房间消息处理器
 * @author: pangcheng
 * @time: 2023/6/12 14:21
 */
public class InitRoomCmdHandler implements CmdHandler {

    @Override
    public void doHandle(Msg msg, GamePanel gamePanel, RoomPanel roomPanel) {
        List<RoomMsgData> roomMsgDatas = JSON.parseObject(msg.getData(), new TypeReference< List<RoomMsgData>>(){});
        roomPanel.removeAll();
        roomMsgDatas.forEach(a->{
            RoomButton button = new RoomButton(a.getRoomId(),a.getMaxUserSize(),a.getUserSize(),a.getFullUser(),a.getStartGame());
            roomPanel.add(button);
        });
        // 添加组件后,需要调用validate()通知布局管理器组件已添加, repaint()重绘容器。如果不调用,组件不会显示
        roomPanel.validate();
        roomPanel.repaint();
    }
}
