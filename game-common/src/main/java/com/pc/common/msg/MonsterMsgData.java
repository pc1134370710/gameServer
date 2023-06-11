package com.pc.common.msg;

import com.pc.common.Constant;
import lombok.Data;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:  小怪npc 消息体
 * @author: pangcheng
 * @time: 2023/6/2 16:38
 */
@Data
public class MonsterMsgData {
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
    private int moveSpeed=2;
    private int randAction=0;

    /**
     * 小怪感知范围， 感知
     */
    private int  senseRange =500;


    /**
     * 玩家在小怪的 攻击范围内
     */
    private int  attackRange =10;

    /**
     *  用于判断小怪正在追踪那个玩家
     */
    private String userId;

    /**
     * 是否在追踪玩家
     */
    private AtomicBoolean isChasing = new AtomicBoolean(false);

    public MonsterMsgData() {
        monsterId = UUID.randomUUID().toString();
        this.monsterX = Constant.withe-50;
        this.monsterY = 10;
    }





    /**
     * 计算 与玩家的距离
     * @param userX
     * @param userY
     * @return
     */
    public int countDistance(int userX, int userY){
        // 求直角坐标系中 两点之间的距离
        int x = userX - monsterX;
        int y = userY - monsterY;
        // 计算好距离
        double sqrt = Math.sqrt((x * x + y * y));
        return (int) sqrt;
    }


    public MonsterMsgData randomXY(){
        Random random = new Random();
        int x = random.nextInt(moveSpeed);
        int y = random.nextInt(moveSpeed);
        if (x % 2 == 0 ) {
             x = -x;
        }
        if (y % 2 == 0 ) {
            y= -y;
        }
        this.monsterX+=x*moveSpeed ;
        this.monsterY+=y*moveSpeed;
        // 判断边界
        if( this.monsterX >= Constant.withe){
            this.monsterX = Constant.withe;
        }
        if(this.monsterY >= Constant.height){
            this.monsterY = Constant.height ;
        }
        // 坐标轴 0，0 在左上角
        if(this.monsterX <=0){
            this.monsterX = 0 ;
        }

        if(this.monsterY <=0){
            this.monsterY = 0 ;
        }
        return this;
    }
}
