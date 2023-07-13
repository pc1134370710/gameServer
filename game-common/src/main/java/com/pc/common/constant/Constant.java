package com.pc.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: pangcheng
 * @create: 2023-06-01 22:44
 **/
public class Constant {

    public static final String HOST = "game.server.host";
    public static final String PORT = "game.server.port";

    /**
     * 游戏背景
     */
    public static final String gameImagePath = "/bg3.jpg";

    public static final String userAttackImageLeft = "/a1-L2.png";
    public static final String userAttackImageRight = "/a1-R2.png";

    /**
     * 技能图片
     */
    public static final String jnRightPath = "/3.png";
    public static final String jnLeftPath = "/4.png";

    public static final List<String> userRichtList = Arrays.asList("/吕布1.png", "/吕布2.png", "/吕布3.png", "/吕布4.png", "/吕布5.png", "/吕布6.png", "/吕布7.png", "/吕布8.png");
    public static final List<String> userLeftList = Arrays.asList("/吕布L1.png", "/吕布L2.png", "/吕布L3.png", "/吕布L4.png", "/吕布L5.png", "/吕布L6.png", "/吕布L7.png", "/吕布L8.png");

    public static final List<String> userAttackLeftList = Arrays.asList("/a1-L0.png", "/a1-L1.png", "/a1-L2.png", "/a1-L3.png", "/a1-L4.png");
    public static final List<String> userAttackRightList = Arrays.asList("/a1-R0.png", "/a1-R1.png", "/a1-R2.png", "/a1-R3.png", "/a1-R4.png");

    public static final String slideLeft = "/a1-L4.png";
    public static final String slideRight = "/a1-R4.png";


    /**
     * 玩家移动速度
     */
    public static final int userRoleSpeed = 4;
    public static final int NPCSpeed = 2;
    /**
     * 闪现 距离
     */
    public static final int SELIDE_SEPEDD = 300;
    /**
     * 技能血量
     */
    public static final int userRoleHP = 100;
    /**
     * 技能蓝条
     */
    public static final int userRoleMP = 100;

    /**
     * 技能伤害值
     */
    public static final int skillHarm = 10;
    /**
     * 普通攻击伤害值
     */
    public static final int normalAttackHarm = 4;
    /**
     * 技能移动速度
     */
    public static final int SKillSpeed = 4;

    /**
     * 电脑玩家 感知范围
     */
    public static final int senseRange = 1500;
    /**
     * 电脑玩家攻击距离
     */
    public static final int attackRange = 130;

    /**
     * 房间选择大厅大小
     */
    public static final int JFrameWithe = 758;
    public static final int JFrameHeight = 442;

    /**
     * 游戏窗口大小
     */
    public static final int withe = 958;
    public static final int height = 600;


    public static Long pcAttackTimeGap = 2000L;
}
