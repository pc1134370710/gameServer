package com.pc.client.gui;

import com.pc.client.cache.LocalGameInfo;
import com.pc.common.ServerCmd;
import com.pc.common.msg.Msg;
import com.pc.common.msg.RoomMsgData;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @description:
 * @author: pangcheng
 * @create: 2023-06-03 13:34
 **/
@Data
public class RoomButton  extends JButton  implements MouseListener{

    private String roomId;
    private Integer maxUserSize;
    /**
     * 当前用户人数
     */
    private Integer userSize;

    /**
     * 是否已经满人了
     */
    private Boolean fullUser;
    private Boolean startGame;

    public RoomButton(RoomMsgData roomMsgData){
        this.roomId = roomMsgData.getRoomId();
        this.maxUserSize = roomMsgData.getMaxUserSize();
        this.userSize = roomMsgData.getUserSize();
        this.fullUser=roomMsgData.getFullUser();
        this.startGame=roomMsgData.getStartGame();
        String text =roomMsgData.getRoomName()+" 房间id: "+roomId+", 满人"+maxUserSize+"开始,当前人数: "+userSize;
        if(startGame){
            text ="正在游戏";
        }else{
            // 没有满，还可以点击
            addMouseListener(this);
            setFocusable(false);
        }
        setText(text);

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) { // 按下鼠标左键
            LocalGameInfo.roomId = roomId;
            setText("已加房间：" + roomId);
            // 获取父级容器
            Container parent = this.getParent();
            RoomPanel roomPanel = (RoomPanel) parent;
            // 初始化游戏连接
            LocalGameInfo.client.initGameChannel();
            // 弹出游戏面板
            // 隐藏房间面板
            LocalGameInfo.roomJFrame.setVisible(false);
            roomPanel.getGameFrame().setVisible(true);
            roomPanel.getGameFrame().setTitle("玩家：" + LocalGameInfo.userId +"， 房间："+roomId);

            Msg msg = new Msg();
            msg.setCmd(ServerCmd.USER_INTO_ROOM.value);
            msg.setUserId(LocalGameInfo.userId);
            msg.setRoomId(roomId);
            LocalGameInfo.client.sendMsg(msg);

            LocalGameInfo.client.sendChatMsg(Msg.getMsg(ServerCmd.INIT_CHAT.getValue(),LocalGameInfo.userId,roomId,null));
            LocalGameInfo.client.registerTask(Msg.getMsg(ServerCmd.REGISTER_TASK.getValue(),LocalGameInfo.userId,roomId,null));


        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {

    }
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
