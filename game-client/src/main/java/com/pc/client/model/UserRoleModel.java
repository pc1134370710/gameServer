package com.pc.client.model;

import com.pc.client.cache.LocalGameInfo;
import com.pc.common.Constant;
import com.pc.common.ImageUtils;
import com.pc.common.msg.UserRoleMsgData;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 用户角色
 * @author: pangcheng
 * @create: 2023-06-02 23:28
 **/
@Data
public class UserRoleModel  extends BasicModel {



    private BufferedImage image;

    private List<BufferedImage> list;
    /**
     *  向左走
     */
    private List<BufferedImage> leftImage;
    /**
     * 像右走
     */
    private List<BufferedImage> rightImage;

    private String userId;

    /**
     * 当前人物方向
     * -1 指向左边， 1指向右边
     */
    private Integer direction;

    /**
     *  移动速度
     */
    private int moveSpeed;
    /**
     * 用户血量
     */
    private int hp;
    /**
     * 魔力值，满了可以释放技能
     */
    private int mp;
    /**
     * 横纵坐标
     */
    private int X;
    private int Y;

    /**
     * 是否发动普通攻击
     */
    private AtomicBoolean attack = new AtomicBoolean(false);
    private AtomicBoolean isOver = new AtomicBoolean(false);

    /**
     *  改变图片
     */
    public void changeImage(){
        if(direction == null) direction = 1;
        if(direction == -1){
            list = leftImage;
        }else{
            list = rightImage;
        }
    }



    public UserRoleModel(){
//        userId = UUID.randomUUID().toString();
        // 读取图片
        try {
            this.list = new CopyOnWriteArrayList<>();
            this.leftImage = new CopyOnWriteArrayList<>();
            this.rightImage = new CopyOnWriteArrayList<>();

            for(String path : Constant.userLeftList){
                BufferedImage monsterAction  = ImageUtils.getImageFromResources(path);
                this.leftImage.add(monsterAction);
            }

            for(String path : Constant.userRichtList){
                BufferedImage monsterAction  = ImageUtils.getImageFromResources(path);
                this.rightImage.add(monsterAction);
            }

            // 攻击资源放在最后一个
            this.leftImage.add(ImageUtils.getImageFromResources(Constant.userAttackImageLeft));
            this.rightImage.add(ImageUtils.getImageFromResources(Constant.userAttackImageRight));
            this.list.addAll(this.rightImage);

            this.X = Constant.withe/2;
            this.Y = Constant.height/2;

        } catch (Exception e) {
            System.out.println("读取小怪图片异常");
            e.printStackTrace();
        }

    }

    public void move(){
        changeImage();
        int len = list.size()-1;
        int randAction =  ThreadLocalRandom.current().nextInt(len);
        image = list.get(randAction%len);
        if(attack.get()){
            // 如果是攻击新形态， 直接获取数组最后一个
            image = list.get(len);
        }
    }
    @Override
    public void paintOneself() {
        // todo 已经死亡了， 不在移动， 图片变换为死亡图片
        if(isOver.get()){
            this.gameGraphics2D.setColor(Color.red);
            this.gameGraphics2D.drawString("玩家:"+this.userId+"gg了",this.X  ,this.Y);
            return;
        }

        move();
        // 加载
        this.gameGraphics2D.setColor(Color.BLUE);
        this.gameGraphics2D.setFont(new Font("user",1,12));
        this.gameGraphics2D.drawString("玩家:"+this.userId,this.X  ,this.Y-20);
        this.gameGraphics2D.setColor(Color.red);
        this.gameGraphics2D.drawString("HP:"+this.hp+" MP:"+this.mp,this.X  ,this.Y-10);
        this.gameGraphics2D.setColor(Color.RED);

        // 绘画边框的代码，先暂时去除
//        if(!attack.get()){
//            // 不攻击
//            this.gameGraphics2D.drawRect(X,Y-8,40,50);
//        }else{
//            if(direction>0){
//                // 往右边
//                this.gameGraphics2D.drawRect(X,Y-8,64,50);
//            }else{
//                // 往左边
//                this.gameGraphics2D.drawRect(X-25,Y-8,64,50);
//            }
//
//        }
        // 向左边按普通攻击的时候，调整一下x
        int tempX=0;
        if(direction<0 && attack.get()){
            tempX=-25;
        }
        this.gameGraphics2D.drawImage(this.image,this.X +tempX ,this.Y,null);
    }

    public Rectangle getRectangle() {
        if(!attack.get()){
            return new Rectangle(X,Y-8,40,50);
        }
        else{
            if(direction>0){
                // 往右边
                return new Rectangle(X,Y-8,64,50);
            }else{
                // 往左边
                return new Rectangle(X-25,Y-8,64,50);
            }
        }
    }


    /**
     * 解析数据包
     * @param userRoleMoveMsgData
     */
   public void analysisMsg(UserRoleMsgData userRoleMoveMsgData){

       if(userRoleMoveMsgData.getUserX() !=null){
           this.X = userRoleMoveMsgData.getUserX();
       }
       if(userRoleMoveMsgData.getUserY() != null){
           this.Y = userRoleMoveMsgData.getUserY();
       }

       if(userRoleMoveMsgData.getDirection()!=null){
           this.direction = userRoleMoveMsgData.getDirection();
       }
       this.userId = userRoleMoveMsgData.getUserId();
       if(userRoleMoveMsgData.getMoveSpeed()!= null){
           this.moveSpeed= userRoleMoveMsgData.getMoveSpeed();
       }
       if(userRoleMoveMsgData.getHp()!= null){
           this.hp= userRoleMoveMsgData.getHp();
       }
       if(userRoleMoveMsgData.getMp()!= null){
           this.mp= userRoleMoveMsgData.getMp();
       }
       if(userRoleMoveMsgData.getAttack()!= null){
           this.attack.set(userRoleMoveMsgData.getAttack());
       }
       if(userRoleMoveMsgData.getIsOver() != null){
           this.isOver.set(userRoleMoveMsgData.getIsOver());
           // 如果是自己的 同时更新本地
           if(userRoleMoveMsgData.getUserId().equals(LocalGameInfo.userId)){
               LocalGameInfo.gameOver.set(userRoleMoveMsgData.getIsOver());
           }
       }


   }


}
