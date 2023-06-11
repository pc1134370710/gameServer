package com.pc.common;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: pangcheng
 * @create: 2023-06-01 22:44
 **/
public class Constant {

    /**
     *  游戏背景
     */
    public static final String gameImagePath = "/背景.png";
    /**
     * 小怪 图片
     */
    public static final String monsterImagePath = "/鸟2.png";

    public static final String  userAttackImageLeft ="/恐龙gj.png";
    public static final String  userAttackImageRight ="/恐龙gj2.png";

    /**
     * 技能图片
     */
    public static final String jnRightPath = "/3.png";
    public static final String jnLeftPath = "/4.png";
    /**
     * 小怪动作
     */
    public static final List<String> actionList = Arrays.asList("/鸟1.png"  ,"/鸟2.png");
    public static final List<String> userRichtList = Arrays.asList("/恐龙1.png"  ,"/恐龙2.png","/恐龙3.png","/恐龙a.png","/恐龙b.png");
    public static final List<String>  userLeftList = Arrays.asList("/f/恐龙1.png"  ,"/f/恐龙2.png","/f/恐龙3.png","/f/恐龙a.png","/f/恐龙b.png");




    /**
     * 玩家移动速度
     */
    public static final int userRoleSpeed=3;
    /**
     * 技能血量
     */
    public static final int userRoleHP=100;
    /**
     * 技能蓝条
     */
    public static final int userRoleMP=100;

    /**
     * 技能伤害值
     */
    public static final int skillHarm =10;
    /**
     * 普通攻击伤害值
     */
    public static final int normalAttackHarm =4;
    /**
     * 技能移动速度
     */
    public static final int SKillSpeed = 4;


    /**
     * 房间选择大厅大小
     */
    public static final int JFrameWithe = 758;
    public static final int JFrameHeight = 442;

    /**
     * 游戏窗口大小
     */
    public static final  int withe = 958 ;
    public static final  int height = 742;


}
