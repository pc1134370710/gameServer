package com.pc.client.model.gui;


import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.client.model.GameBackdrop;
import com.pc.client.model.NpcMonster;
import com.pc.client.model.SkillModel;
import com.pc.client.model.UserRoleModel;
import com.pc.common.Constant;
import com.pc.common.ServerCmd;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;
import com.pc.common.msg.UserRoleMsgData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:  游戏面板
 * @author: pangcheng
 * @time: 2023/6/1 19:10
 */
public class GamePanel extends JPanel implements KeyListener {


    /**
     * 画布
     *  用户绘画游戏面板的图片
     */
    private BufferedImage bufferedImage;
    /**
     *  绘图笔
     */
    private Graphics2D graphics2D ;

    /**
     * 游戏背景
     */
//    private GameBackdrop gameBackdrop;

    /**
     * 是否已经初始化完毕
     */
    private AtomicBoolean init = new AtomicBoolean(false);


    public GamePanel() {
        addKeyListener(this);
        setFocusable(false);
    }

    /**
     * 	重写 paint 方法 即 重写画布的绘画方法
     * 	将图片绘制到面板中
     * 	然后可以用该对像进行2D画图
     * 	该对象 会自动调用
     * @param g  the <code>Graphics</code> context in which to paint
     * 1. 第一次显示面板时。当面板第一次显示在屏幕上时,paint方法会被调用来绘制面板。
     * 2. 面板大小改变时。如果面板的大小由于用户调整窗口大小而改变,paint方法会被调用来重绘面板。
     * 3. 面板重绘时。如果由于某些原因需要重绘面板,可以调用面板的repaint()方法,这会导致paint方法被调用。
     * 4. 窗口最小化后恢复时。如果用户将包含面板的窗口最小化,然后再恢复,paint方法会被调用来重绘面板。
     * 5. 面板的setBounds()或setSize()方法被调用后。如果通过代码调用setBounds()或setSize()改变面板大小,paint方法会被调用。
     * 6. 面板的父容器大小改变时。如果面板的父容器大小改变,很可能会影响到面板自身的显示,所以paint方法会被调用。
     * 7. 面板的背景或前景色改变时。如果面板的背景或前景色改变,paint方法会被调用来绘制新的颜色。
     * 8. 窗口或应用程序被覆盖后再显示出来时。如果有其他窗口或应用程序覆盖了包含面板的窗口,然后再将其显示出来,paint方法会被调用。
     */
    @Override
    public void paint(Graphics g) {
//        System.out.println("开始绘制游戏面板");
        // 初始化游戏
        initPanel();

        this.graphics2D.setColor(Color.lightGray);
        this.graphics2D.fillRect(0,0,Constant.withe,Constant.height);
        for(NpcMonster npcMonster: LocalGameInfo.npcMonsters.values()){
            npcMonster.paintOneself();
        }

        // 绘制用户
        for(UserRoleModel userRoleModel : LocalGameInfo.userRoleModelMap.values()){
            userRoleModel.paintOneself();
        }

        Map<String, SkillModel> tempSkill =  new ConcurrentHashMap<>();
        // 绘制技能
        for(SkillModel skillModel : LocalGameInfo.stringSkillModelMap.values()){
            // 绘制有效技能
            if(skillModel.checkSkill()){
                skillModel.paintOneself();
                tempSkill.put(skillModel.getSkillId(),skillModel);
            }
        }
        LocalGameInfo.stringSkillModelMap = tempSkill;
        // 从窗体左上角开始全部的画图， 将图画入画布中
        g.drawImage(bufferedImage, 0, 0, this);

    }


    /**
     * 初始化比较耗时 放入构造函数中 会导致 paint() 不能够被触发
     */
    private  void initPanel(){
        if(!init.get()){
            // 如果还没有初始化
            synchronized (GamePanel.class){
                if(init.get()){
                    return;
                }
                System.out.println("首次启动游戏初始化游戏面板");
                // 初始化游戏画板
                // bgr颜色模式   定义画布大小
                this.bufferedImage   = new BufferedImage(Constant.withe, Constant.height, BufferedImage.TYPE_3BYTE_BGR);
                // 返回:一个Graphics2D，用于绘制到此图像中
                this.graphics2D = bufferedImage.createGraphics();
                this.graphics2D.setColor(Color.GRAY);
                this.graphics2D.drawRect(0,0,Constant.withe,Constant.height);
                // 初始化背景，将背景，
//                gameBackdrop = new GameBackdrop(this.graphics2D);
                init.compareAndSet(false,true);

            }
        }

    }



    /**
     * 添加小怪
     * @param npcMonster
     */
    public void addMonsters(NpcMonster npcMonster){
        npcMonster.setGameGraphics2D(this.graphics2D);
        LocalGameInfo.npcMonsters.put(npcMonster.getMonsterId(),npcMonster);
    }
    public void addUser(UserRoleModel userRoleModel){
        userRoleModel.setGameGraphics2D(this.graphics2D);
        LocalGameInfo.userRoleModelMap.put(userRoleModel.getUserId(),userRoleModel);
    }
    public void addSkill(SkillModel skillModel){
        skillModel.setGameGraphics2D(this.graphics2D);
        LocalGameInfo.stringSkillModelMap.put(skillModel.getSkillId(),skillModel);
    }



    @Override
    public void keyTyped(KeyEvent e) {

    }
    /**
     * 监听键盘事件  // 获取按下的键盘的 编码
     * @param e
     */
    boolean keyWPressed = false;
    boolean keyAPressed = false;
    boolean keySPressed = false;
    boolean keyDPressed = false;

    // 防止一直 按住攻击
    boolean keyJPressed = false;
    @Override
    public void keyPressed(KeyEvent e) {
        // 玩家死亡游戏结束了， 不给按按键
        if(LocalGameInfo.gameOver.get()){
            return;
        }

        int code = e.getKeyCode();

        // 如果一直按住 普通攻击，取消
        if(keyJPressed){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            UserRoleMsgData userRoleMsgData = new UserRoleMsgData();
            userRoleMsgData.setAttack(false);
//            userRoleMsgData.setUserX(userRoleModel.getX());
//            userRoleMsgData.setUserY(userRoleModel.getY());
            userRoleMsgData.setUserId(userRoleModel.getUserId());
            Msg msg = Msg.getMsg(ServerCmd.USER_ATTACK.value, userRoleModel.getUserId(), userRoleMsgData);
            msg.setRoomId(LocalGameInfo.roomId);
            LocalGameInfo.client.sendMsg(msg);
        }
        // 普通攻击
        if(code == KeyEvent.VK_J && keyJPressed==false){
            keyJPressed = true;
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            UserRoleMsgData userRoleMsgData = new UserRoleMsgData();
            userRoleMsgData.setAttack(true);
            userRoleMsgData.setUserId(userRoleModel.getUserId());
            Msg msg = Msg.getMsg(ServerCmd.USER_ATTACK.value, userRoleModel.getUserId(), userRoleMsgData);
            msg.setRoomId(LocalGameInfo.roomId);
            LocalGameInfo.client.sendMsg(msg);

        }

        // 释放技能
        if(code == KeyEvent.VK_K){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            if(userRoleModel.getMp()>=Constant.userRoleMP){
                // 释放完技能客户端归0
                userRoleModel.setMp(0);
                SkillMsgData skillMsgData = new SkillMsgData();
                skillMsgData.setUserId(LocalGameInfo.userId);
                Msg msg = Msg.getMsg(ServerCmd.USER_ROLE_SKILL.getValue(), LocalGameInfo.userId, skillMsgData);
                msg.setRoomId(LocalGameInfo.roomId);
                LocalGameInfo.client.sendMsg(msg);
            }
            // 蓝量不足

        }
        // 往右上走
        if(keyWPressed && keyDPressed){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX()+userRoleModel.getMoveSpeed(),userRoleModel.getY()-userRoleModel.getMoveSpeed(),null);
            // 这里 return 为了防止跟 单纯的上下左右冲突
            return;
        }
        // 往右下走
        if(keySPressed && keyDPressed){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX()+userRoleModel.getMoveSpeed(),userRoleModel.getY()+userRoleModel.getMoveSpeed(),null);
            return;
        }
        // 往坐上走
        if(keyWPressed && keyAPressed){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX()-userRoleModel.getMoveSpeed(),userRoleModel.getY()-userRoleModel.getMoveSpeed(),null);
            return;
        }
        // 往左下走
        if(keySPressed && keyAPressed){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX()-userRoleModel.getMoveSpeed(),userRoleModel.getY()+userRoleModel.getMoveSpeed(),null);
            return;
        }

        if(code == KeyEvent.VK_W){
            keyWPressed=true;
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX(),userRoleModel.getY()-userRoleModel.getMoveSpeed(),null);
        }
        if(code == KeyEvent.VK_S){
            keySPressed=true;
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX(),userRoleModel.getY()+userRoleModel.getMoveSpeed(),null);
        }
        if(code == KeyEvent.VK_A){
            keyAPressed=true;
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX()-userRoleModel.getMoveSpeed(),userRoleModel.getY(),-1);
        }
        if(code == KeyEvent.VK_D){
            keyDPressed=true;
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            sendUserMoveMsg(userRoleModel.getX()+userRoleModel.getMoveSpeed(),userRoleModel.getY(),1);
        }




    }

    @Override
    public void keyReleased(KeyEvent e) {
        // 玩家死亡游戏结束了， 不给按按键
        if(LocalGameInfo.gameOver.get()){
            return;
        }
        // 松开键盘
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_W){
            keyWPressed=false;
        }
        if(code == KeyEvent.VK_S){
            keySPressed=false;
        }
        if(code == KeyEvent.VK_A){
            keyAPressed=false;
        }
        if(code == KeyEvent.VK_D) {
            keyDPressed = false;
        }
        // 松开普通攻击
        if(code == KeyEvent.VK_J){
            UserRoleModel userRoleModel = LocalGameInfo.userRoleModelMap.get(LocalGameInfo.userId);
            UserRoleMsgData userRoleMsgData = new UserRoleMsgData();
            userRoleMsgData.setAttack(false);
            userRoleMsgData.setUserX(userRoleModel.getX());
            userRoleMsgData.setUserY(userRoleModel.getY());
            userRoleMsgData.setUserId(userRoleModel.getUserId());
            Msg msg = Msg.getMsg(ServerCmd.USER_ATTACK.value, userRoleModel.getUserId(), userRoleMsgData);
            msg.setRoomId(LocalGameInfo.roomId);
            LocalGameInfo.client.sendMsg(msg);
            keyJPressed = false;
        }
    }

    /**
     * 发送角色移动消息
     * @param userX
     * @param userY
     * @param direction 方向 -1 左边。 1 右边
     */
    private void sendUserMoveMsg(Integer userX,Integer userY,Integer direction){
        Msg msg = new Msg();
        msg.setRoomId(LocalGameInfo.roomId);
        msg.setUserId(LocalGameInfo.userId);
        msg.setCmd(ServerCmd.USER_MOVE.getValue());
        UserRoleMsgData userRoleMoveMsgData = new UserRoleMsgData();
        userRoleMoveMsgData.setUserX( userX);
        userRoleMoveMsgData.setUserY( userY);
        userRoleMoveMsgData.setUserId(LocalGameInfo.userId);
        userRoleMoveMsgData.limitingXY();
        // 设置移动方向
        if(direction!=null){
            userRoleMoveMsgData.setDirection(direction);
        }
        msg.setData(JSON.toJSONString(userRoleMoveMsgData));
        LocalGameInfo.client.sendMsg(msg);
    }

}
