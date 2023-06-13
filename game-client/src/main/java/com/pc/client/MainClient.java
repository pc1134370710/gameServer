package com.pc.client;

import com.pc.client.cache.LocalGameInfo;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.LoginUserPanel;
import com.pc.client.gui.RoomPanel;
import com.pc.common.Constant;
import com.pc.common.PropertiesUtils;
import com.pc.common.ServerCmd;
import com.pc.common.msg.Msg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @description:  客户端主程序 运行入口
 * @author: pangcheng
 * @time: 2023/6/1 18:58
 */
public class MainClient {


    public static void main(String[] args) {
        PropertiesUtils.load();

        JFrame frame = getCommonJFrame("");
        frame.setResizable(false); // 固定窗口大小
        LocalGameInfo.jFrame = frame;

        LoginUserPanel loginUserPanel = new LoginUserPanel();
        loginUserPanel.setBackground(Color.cyan);
        loginUserPanel.setBounds(0,0, Constant.JFrameWithe,Constant.JFrameHeight);
        frame.add(loginUserPanel);

        JLabel jLabel = new JLabel("游戏名");
        jLabel.setForeground(Color.BLACK);
        JTextField jTextField = new JTextField();

        loginUserPanel.add(jLabel);
        jLabel.setBounds(50,100,100,50);
        loginUserPanel.add(jTextField);
        jTextField.setBounds(150,100,100,50);
        JButton jButton = new JButton("确定");

        loginUserPanel.add(jButton);
        jButton.setBounds(100,200,150,50);
        jButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // 按下鼠标左键

                    // todo 待检测 用户名是否合法，


                    LocalGameInfo.userId = jTextField.getText();
                    frame.setVisible(false);

                    // 创建房间面板
                    JFrame roomJFrame = getCommonJFrame("当前玩家：" + LocalGameInfo.userId);
                    roomJFrame.setResizable(false); // 固定窗口大小

                    RoomPanel roomPanel = new RoomPanel();
//                    roomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                    roomJFrame.add(roomPanel);
                    roomPanel.setBounds( 0, 0 ,Constant.JFrameWithe, Constant.JFrameHeight);
                    LocalGameInfo.roomJFrame = roomJFrame;

                    // 创建游戏面板
                    JFrame gameFrame = getGameJFrame();
                    LocalGameInfo.gameFrame = gameFrame;
                    // 开始是不可见的
                    gameFrame.setVisible(false);
                    // 创建游戏面板
                    GamePanel gamePanel  = new GamePanel();
                    gameFrame.add(gamePanel);
                    // 监听事件添加一个键盘监听事件
                    gameFrame.addKeyListener(gamePanel);
                    gamePanel.setBounds(0, 0, Constant.withe, Constant.height); // 设置游戏面板在 面板 bigpanl 中的位置跟大小
                    new Thread(()->{
                        while (true){
                            // 刷新游戏界面
                            gamePanel.repaint();
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException r) {
                                r.printStackTrace();
                            }
                        }
                    }).start();

                    roomPanel.setGameFrame(gameFrame);
                    LocalGameInfo.gamePanel = gamePanel;
                    LocalGameInfo.roomPanel = roomPanel;
                    // 启动游戏客户端
                    LocalGameInfo.client = new Client();

                    Msg msg = new Msg();
                    msg.setCmd(ServerCmd.GET_ALL_ROOM.getValue());
                    LocalGameInfo.client.sendRoomMsg(msg);
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
        });

    }

    /**
     * 创建游戏面板
     * @return
     */
    public static JFrame getGameJFrame(){
        JFrame frame = new JFrame("玩家：" + LocalGameInfo.userId +"， 房间："+ LocalGameInfo.roomId);
        // 绝对布局
        frame.setLayout(null);
//        frame.setResizable(false); // 固定窗口大小
        // 设置框架的大小
        frame.setSize(Constant.withe,Constant.height);
        //设置特定组件相关的框架位置，若值为null,那么框架屏幕居中
        frame.setLocationRelativeTo(null);
        // 关闭窗口时退出程序, 指定框架关闭时的操作
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        // 显示还是隐藏窗口
        frame.setVisible(true);

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }
            @Override
            public void windowClosing(WindowEvent e) {

                Msg exitGame = Msg.getMsg(ServerCmd.EXIT_GAME.getValue(), LocalGameInfo.userId, null);
                exitGame.setRoomId(LocalGameInfo.roomId);
                LocalGameInfo.client.sendRoomMsg(exitGame);
                // 重置房间号
                LocalGameInfo.clear();

                // 显示房间面板
                LocalGameInfo.roomJFrame.setVisible(true);
                Msg msg = new Msg();
                msg.setCmd(ServerCmd.GET_ALL_ROOM.getValue());
                LocalGameInfo.client.sendRoomMsg(msg);

                // 刷新游戏连接
                LocalGameInfo.client.refreshGameChannel();
            }
            @Override
            public void windowClosed(WindowEvent e) {

            }
            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        return frame;
    }

    /**
     * 创建普通 面板
     * @param name
     * @return
     */
    public static JFrame getCommonJFrame(String name){
        JFrame frame = new JFrame(name);
        // 绝对布局
        frame.setLayout(null);
//        frame.setResizable(false); // 固定窗口大小
        // 设置框架的大小
        frame.setSize(Constant.JFrameWithe,Constant.JFrameHeight);
        //设置特定组件相关的框架位置，若值为null,那么框架屏幕居中
        frame.setLocationRelativeTo(null);
        // 关闭窗口时退出程序, 指定框架关闭时的操作
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 显示还是隐藏窗口
        frame.setVisible(true);
        return frame;
    }
}