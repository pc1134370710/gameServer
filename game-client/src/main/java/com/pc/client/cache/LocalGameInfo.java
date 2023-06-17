package com.pc.client.cache;

import com.pc.client.Client;
import com.pc.client.gui.ChatPanel;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomPanel;
import com.pc.client.model.SkillModel;
import com.pc.client.model.UserRoleModel;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 客户端本地游戏存放信息
 * @author: pangcheng
 * @create: 2023-06-03 13:52
 **/
public class LocalGameInfo {

    /**
     * 客户端本地用户信息
     */
    public volatile static String userId;

    /**
     * 当前所加入的游戏房间号
     */
    public volatile static String roomId;


    /**
     * 本地连接客户端
     */
    public volatile static Client client;



    /**
     * 游戏房间用户
     */
    public volatile static Map<String, UserRoleModel> userRoleModelMap = new ConcurrentHashMap<>();
    /**
     * 游戏房间技能集合
     */
    public volatile static Map<String, SkillModel> stringSkillModelMap = new ConcurrentHashMap<>();

    /**
     * 游戏是否结束
     */
    public volatile static AtomicBoolean gameOver = new AtomicBoolean(false);


    /**
     * 登录窗口
     */
    public volatile static  JFrame jFrame;
    /**
     * 房间窗口
     */
    public volatile static  JFrame roomJFrame;

    /**
     * 游戏窗口
     */
    public volatile static JFrame gameFrame;

    /**
     * 游戏画板
     */
    public volatile static GamePanel gamePanel;
    /**
     * 房间画板
     */
    public volatile static RoomPanel roomPanel;

    /**
     * 聊天窗口面板
     */

    public volatile static ChatPanel chatPanel;




    public static void clear(){
        LocalGameInfo.roomId=null;
        LocalGameInfo.gameOver.set(false);
        LocalGameInfo.userRoleModelMap.clear();
    }

}
