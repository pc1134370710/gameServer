package com.pc.client.gui;

import com.pc.client.cache.LocalGameInfo;
import com.pc.common.prtotcol.ServerCmd;
import com.pc.common.msg.Msg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description: 聊天面板
 * @author: pangcheng
 * @create: 2023-06-17 12:22
 **/

public class ChatPanel extends JPanel {
    /**
     * 聊天显示列表
     */
    JTextArea textArea;
    JScrollPane scrollPane;

    // 创建输入框和按钮
    JTextField textField ;
    JButton button ;

    public ChatPanel() {
        setFocusable(false);
        setLayout(new BorderLayout());
        // 用于实现 右边的聊天窗口
        // 创建文本域
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        // 创建滚动条并将文本域添加到滚动条
        scrollPane = new JScrollPane(textArea);
        button = new JButton("发送");

        textField = new JTextField();

        add(scrollPane, BorderLayout.CENTER);

        JPanel jPanel = new JPanel();
        jPanel. setLayout(new BorderLayout());
        jPanel.add(textField, BorderLayout.WEST);
        jPanel.add(button, BorderLayout.EAST);
        textField.setPreferredSize(new Dimension(300, 40));
        add(jPanel,BorderLayout.SOUTH);
        button.setFocusable(false);
        textField.setFocusable(true);

        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 让输入框可以重新获取焦点
                textField.setFocusable(true);

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // 按下鼠标左键
                    String text = textField.getText();
                    textField.setText("");
                    if(text == null){
                        return;
                    }
                    if(text.trim().length() ==0){
                        return;
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = simpleDateFormat.format(new Date());
                    text = "(玩家) "+LocalGameInfo.userId+"："+text +"  " + format;
                    // 发送房间内聊天信息
                    LocalGameInfo.client.sendChatMsg(Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), LocalGameInfo.userId,LocalGameInfo.roomId,text));
                    textField.setFocusable(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 游戏面板获取焦点
                LocalGameInfo.gamePanel.setFocusable(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });


    }

    /**
     * 追加文本
     * @param text
     */
    public void addText(String text){
        textArea.append(text+"\n");
    }
}
