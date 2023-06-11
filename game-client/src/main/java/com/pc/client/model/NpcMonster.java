package com.pc.client.model;

import com.pc.common.Constant;
import com.pc.common.ImageUtils;
import com.pc.common.msg.MonsterMsgData;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: NPC小怪
 * @author: pangcheng
 * @time: 2023/6/1 20:02
 */
@Data
public class NpcMonster extends BasicModel {


    /**
     * 小怪展示图片
     */
    private BufferedImage monsterImage;
    /**
     * 小怪动作图片
     */
    private List<BufferedImage> monsterActions;
    /**
     * 小怪的坐标
     */
    private int monsterX;
    private int monsterY;
    /**
     * 小怪id
     */
    private String monsterId;

    /**
     * 小怪运动速度
     */
    private int moveSpeed;
    private int randAction;

    public NpcMonster(){
        // 读取图片
        try {
            this.monsterActions = new ArrayList<>();
//            this.monsterImage = ImageIO.read(new File(Constant.monsterImagePath));
            this.monsterImage = ImageUtils.getImageFromResources(Constant.monsterImagePath);
            for(String path : Constant.actionList){
                BufferedImage monsterAction  = ImageUtils.getImageFromResources(path);
                this.monsterActions.add(monsterAction);
            }
            this.monsterX = Constant.withe/2;
            this.monsterY = Constant.height/2;
        } catch (Exception e) {
            System.out.println("读取小怪图片异常");
            e.printStackTrace();
        }

    }


    /**
     * 获取小怪Rectangle 用于矩形碰撞检测
     * @return
     */
    public  Rectangle monsterRectangle() {
        return new Rectangle(this.monsterX+20,this.monsterY,20,20);
    }

    @Override
    public void paintOneself() {
        // 加载小怪到游戏面板中
        this.gameGraphics2D.drawImage(this.monsterImage,this.monsterX  ,this.monsterY,null);
    }

    public void analysisMsg(MonsterMsgData monsterMsgData) {
        this.monsterX = monsterMsgData.getMonsterX();
        this.monsterY = monsterMsgData.getMonsterY();
        this.monsterImage = monsterActions.get(monsterMsgData.getRandAction()%monsterActions.size());
        this.monsterId = monsterMsgData.getMonsterId();
        this.moveSpeed = monsterMsgData.getMoveSpeed();
    }
}
