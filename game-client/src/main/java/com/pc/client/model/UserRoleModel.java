package com.pc.client.model;

import com.pc.client.utils.AudioPlayer;
import com.pc.client.cache.LocalGameInfo;
import com.pc.client.utils.ThreadPoolUtils;
import com.pc.common.constant.Constant;
import com.pc.common.util.ImageUtil;
import com.pc.common.msg.UserRoleMsgData;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private List<BufferedImage> attackLeftImage;
    private List<BufferedImage> attackRightImage;

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
    private int userX;
    private int userY;

    /**
     * 是否发动普通攻击
     */
    private AtomicBoolean attack = new AtomicBoolean(false);
    /**
     * 是否滑行
     */
    private AtomicBoolean slide = new AtomicBoolean(false);
    private AtomicBoolean isOver = new AtomicBoolean(false);
    // 随机移动图片
    private AtomicInteger imageRand = new AtomicInteger(0);
    // 图片刷新评率
    private AtomicInteger refresh = new AtomicInteger(0);

    /**
     *  改变图片
     */
    public void changeImage(){
        if(direction == null) {
            direction = 1;
        }
        if(direction == -1){
            list = leftImage;
        }else{
            list = rightImage;
        }
        this.image = list.get(imageRand.get());
    }



    public UserRoleModel(){
        // 读取图片
        try {
            this.list = new CopyOnWriteArrayList<>();
            this.leftImage = new CopyOnWriteArrayList<>();
            this.rightImage = new CopyOnWriteArrayList<>();
            this.attackLeftImage = new CopyOnWriteArrayList<>();
            this.attackRightImage = new CopyOnWriteArrayList<>();

            for(String path : Constant.userLeftList){
                BufferedImage monsterAction  = ImageUtil.getImageFromResourcesLb(path);
                this.leftImage.add(monsterAction);
            }

            for(String path : Constant.userRichtList){
                BufferedImage monsterAction  = ImageUtil.getImageFromResourcesLb(path);
                this.rightImage.add(monsterAction);
            }
            for(String path : Constant.userAttackRightList){
                BufferedImage monsterAction  = ImageUtil.getImageFromResourcesLb(path);
                this.attackRightImage.add(monsterAction);
            }
            for(String path : Constant.userAttackLeftList){
                BufferedImage monsterAction  = ImageUtil.getImageFromResourcesLb(path);
                this.getAttackLeftImage().add(monsterAction);
            }


            this.list.addAll(this.rightImage);
            this.image = rightImage.get(0);
            this.userX = Constant.withe/2;
            this.userY = Constant.height/2;

        } catch (Exception e) {
            System.out.println("读取图片异常");
            e.printStackTrace();
        }

    }

    @Override
    public void paintOneself() {
        // TODO 已经死亡了， 不在移动， 图片变换为死亡图片
        if(isOver.get()){
            this.gameGraphics2D.setColor(Color.red);
            this.gameGraphics2D.drawString("玩家:"+this.userId+" gg了",this.userX  ,this.userY);
            return;
        }


        // 加载
        this.gameGraphics2D.setColor(Color.BLUE);
        this.gameGraphics2D.setFont(new Font("user",1,12));
        this.gameGraphics2D.drawString("玩家:"+this.userId,this.userX  ,this.userY-20);
        this.gameGraphics2D.setColor(Color.red);
        this.gameGraphics2D.drawString("HP:"+this.hp+" MP:"+this.mp,this.userX  ,this.userY-10);
        this.gameGraphics2D.setColor(Color.RED);

        // 绘画边框的代码，先暂时去除
//        if(!attack.get()){
//            // 不攻击
//            this.gameGraphics2D.drawRect(userX,userY-8,45,130);
//        }else{
//            if(direction>0){
//                // 往右边
//                this.gameGraphics2D.drawRect(userX-35,userY-8,200,130);
//            }else{
//                // 往左边
//                this.gameGraphics2D.drawRect(userX-100,userY-8,200,130);
//            }
//
//        }

        // 如果是攻击形态， 则先画完这攻击的图在切换

//         向左边按普通攻击的时候，调整一下x
        int tempY=0;
        if(attack.get()){
            tempY=12;
        }
        this.gameGraphics2D.drawImage(this.image,this.userX-98  ,this.userY+tempY,null);

    }



    /**
     * 解析移动 消息包
     * @param userRoleMoveMsgData
     */
    public void analysisMoveMsg(UserRoleMsgData userRoleMoveMsgData){
        refresh.incrementAndGet();
        refresh.compareAndSet(100,0);
        if(userRoleMoveMsgData.getUserX() !=null){
            this.userX = userRoleMoveMsgData.getUserX();
        }
        if(userRoleMoveMsgData.getUserY() != null){
            this.userY = userRoleMoveMsgData.getUserY();
        }
        if(userRoleMoveMsgData.getDirection()!=null){
            this.direction = userRoleMoveMsgData.getDirection();
            // 改变图片方向
            changeImage();
        }
        // 减少图片刷新评率
        if(refresh.get()%5==0){
            int len = list.size();
            int i = imageRand.incrementAndGet();
            imageRand.compareAndSet(len,0);
            this.image = list.get(i%len);
        }
    }

    /**
     * 解析攻击数据包
     * @param userRoleMoveMsgData
     */
    public void analysisAttackMsg(UserRoleMsgData userRoleMoveMsgData){
        this.attack.set(userRoleMoveMsgData.getAttack());
        if(this.attack.get()){
            // 如果是攻击状态下
            ThreadPoolUtils.threadPoolExecutor.execute(()->{
                // 播放动画
                BufferedImage lastImage = image;
                List<BufferedImage> temp = attackLeftImage;
                if(direction>0){
                    temp = attackRightImage;
                }
                for(BufferedImage bufferedImage : temp){
                    this.attack.set(true);
                    this.image = bufferedImage;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    // 刷新游戏面板
                    LocalGameInfo.gamePanel.repaint();
                }
                // 攻击结束
                this.attack.set(false);
                // 攻击播放玩后， 恢复最后一个
                this.image = lastImage;
                LocalGameInfo.gamePanel.repaint();

            });
            // 播放攻击生效
            AudioPlayer.playerAttack();
        }else {
            // 恢复图片
            this.image = list.get(imageRand.get());
        }
    }
    /**
     * 解析数据包
     * @param userRoleMoveMsgData
     */
   public void analysisMsg(UserRoleMsgData userRoleMoveMsgData){
       this.userId =  userRoleMoveMsgData.getUserId();
       if(userRoleMoveMsgData.getMoveSpeed()!= null){
           this.moveSpeed= userRoleMoveMsgData.getMoveSpeed();
       }
       if(userRoleMoveMsgData.getHp()!= null){
           this.hp= userRoleMoveMsgData.getHp();
       }
       if(userRoleMoveMsgData.getMp()!= null){
           this.mp= userRoleMoveMsgData.getMp();
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
