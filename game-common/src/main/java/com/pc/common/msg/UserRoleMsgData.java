package com.pc.common.msg;

import com.pc.common.constant.Constant;
import lombok.Data;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description: 用户角色数据包
 * @author: pangcheng
 * @create: 2023-06-03 10:49
 **/
@Data
public class UserRoleMsgData {
    /**
     * 坐标
     */
    private Integer userX;
    private Integer userY;
    /**
     * id
     */
    private String userId;
    /**
     * 运动速度
     */
    private Integer moveSpeed;

    /**
     * 用户血量
     */
    private Integer hp;
    /**
     * 魔力值，满了可以释放技能
     */
    private Integer mp;

    /**
     * 发动普通攻击
     */
    private Boolean attack;

    /**
     * -1 往左， 1往右
     */
    private Integer direction;

    /**
     * 是否已经死亡
     */
    private Boolean isOver;
    /**
     * 是否 闪现
     */
    private Boolean slide;


    /**
     * 电脑正在追踪的玩家
     */
    private String chasingUserId;
    private Boolean isNpc;

    /**
     * 电脑玩家当前攻击时间， 用于电脑玩家停止 ，间隔后继续攻击
     */
    private Long pcAttackTime;


    public UserRoleMsgData() {

        // 默认都是 正常状态,因为会自动恢复，所以放在这

        this.moveSpeed = Constant.userRoleSpeed;
    }

    /**
     * 初始化用户坐标， 血量等
     * @return
     */
    public UserRoleMsgData init(){
        this.userX = Constant.withe / 2;
        this.userY = Constant.height / 2;
        this.hp = Constant.userRoleHP;
        this.mp = Constant.userRoleMP;
        this.direction = 1;
        this.isNpc = false;
        this.isOver = false;
        this.slide = false;
        return this;
    }
    // 初始化npc

    public UserRoleMsgData initNpc(){
        Random random = new Random(System.currentTimeMillis());
        int i = random.nextInt(100);
        int y  = ThreadLocalRandom.current().nextInt(Constant.height);
        // 随机左右出现
        this.userX = i%2==0?0:Constant.withe;
        this.userY = y;
        this.hp = Constant.userRoleHP;
        this.mp = Constant.userRoleMP;
        this.direction = 1;
        this.isNpc = true;
        this.isOver = false;
        this.slide = false;
        return this;
    }


    /**
     * 计算 x,y 坐标与现在坐标的距离
     * @param x
     * @param y
     * @return
     */
    public int distanceCalculator(int x, int y){
        // 计算距离公式
        x =userX-x;
        y=userY-y;
        int count = (int) Math.sqrt(x * x +y * y);
        return count;
    }

    /**
     * 限制坐标
     */
    public void limitingXY() {
        // 判断边界
        if (this.userX >= Constant.withe) {
            this.userX = Constant.withe;
        }
        if (this.userY >= Constant.height) {
            this.userY = Constant.height;
        }
        // 坐标轴 0，0 在左上角
        if (this.userX <= 0) {
            this.userX = 0;
        }

        if (this.userY <= 0) {
            this.userY = 0;
        }
    }

    public Boolean getAttack() {
        return attack;
    }

    public Rectangle rectangle() {
        if(attack!=null && attack){
            if(direction !=null && direction<0){
                // 往左边
                return new Rectangle(userX-100,userY-8,200,130);
            }else{
                // 往右边
                return new Rectangle(userX-35,userY-8,200,130);
            }
        }else{
            // 如果不是攻击形态
            return new Rectangle(userX,userY-8,45,130);
        }
    }



    /**
     * 非攻击状态下的矩形
     * @return
     */
    public Rectangle commonRectangle() {
        return new Rectangle(userX,userY-8,45,130);
    }


    /**
     * 攻击状态下的矩形
     * @return
     */
    public Rectangle attackRectangle() {
        if(direction !=null && direction<0){
            // 往左边
            return new Rectangle(userX-100,userY-8,200,130);
        }else{
            // 往右边
            return new Rectangle(userX-35,userY-8,200,130);
        }
    }



}
