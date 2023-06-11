package com.pc.client.model;

import com.pc.common.Constant;
import com.pc.common.ImageUtils;
import com.pc.common.msg.SkillMsgData;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @description: 技能类
 * @author: pangcheng
 * @create: 2023-06-03 10:04
 **/
@Data
public class SkillModel  extends BasicModel{

    private BufferedImage image;

    private BufferedImage leftImage;
    private BufferedImage rightImage;

    private String skillId;

    /**
     * 技能释放者
     */
    private String userId;

    private int X;
    private int Y;

    // 是否往左移动
    /**
     *  -1 往左， 1 往右
     */
    private int isLeftMove;

    //技能移动速度
    private int speed=5;



    /**
     * 检测技能是否有效
     * @return
     */
    public boolean checkSkill(){
        if(X >= Constant.withe || X<=0){
            return false;
        }
        return true;
    }


    public SkillModel(){
        try {
            this.image = ImageUtils.getImageFromResources(Constant.jnRightPath);
            this.leftImage = ImageUtils.getImageFromResources(Constant.jnLeftPath);
            this.rightImage = ImageUtils.getImageFromResources(Constant.jnRightPath);
        } catch (Exception e) {

            System.out.println("加载技能图片失败");
            e.printStackTrace();
        }
    }

    @Override
    public void paintOneself() {
        if(isLeftMove <0){
            this.image = leftImage;
        }else {
            this.image = rightImage;
        }
        this.gameGraphics2D.setColor(Color.RED);
        this.gameGraphics2D.drawRect(X,Y,35,36);
        this.gameGraphics2D.drawImage(image,X,Y,40,40,null);
//        move(); 交由服务器移动技能

    }


    /**
     * 技能移动
     */
    public void move(){
        this.X += speed * isLeftMove;
    }

    public  Rectangle monsterRectangle() {
        // 这里的x y 是
        return new Rectangle(this.X,this.Y,35,36);
    }
    public void analysisMsg(SkillMsgData skillMsgData){
        this.X = skillMsgData.getX();
        this.Y = skillMsgData.getY();
        this.isLeftMove = skillMsgData.getIsLeftMove();
        this.userId = skillMsgData.getUserId();
        this.skillId = skillMsgData.getSkillId();
        this.speed = skillMsgData.getSpeed();
    }
}
