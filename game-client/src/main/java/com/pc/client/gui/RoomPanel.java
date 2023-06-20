package com.pc.client.gui;

import com.pc.client.cache.LocalGameInfo;
import com.pc.common.prtotcol.ServerCmd;
import com.pc.common.msg.Msg;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 房间面板
 * @author: pangcheng
 * @create: 2023-06-03 13:14
 **/
@Data
public class RoomPanel extends Panel {
    /**
     * 游戏面板, 按钮点击后需要 弹出游戏面板
     */
    private JFrame gameFrame;

    /**
     * 当前在线用户数量
     */
    private int zxuserSize=0;

    /**
     * 定时
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);

    public RoomPanel() {
        setFocusable(false);


        scheduledThreadPoolExecutor.scheduleWithFixedDelay(()->{
            Msg msg = new Msg();
            msg.setCmd(ServerCmd.GET_ALL_ROOM.getValue());
            LocalGameInfo.client.sendRoomMsg(msg);

            msg.setCmd(ServerCmd.GET_ONLINE_USER_SIZE.getValue());
            LocalGameInfo.client.sendRoomMsg(msg);

            // 每隔5秒获取一下
        },1,5, TimeUnit.SECONDS);

    }

    @Override
    public void paint(Graphics g) {


        super.paint(g);
    }
}
