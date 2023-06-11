package com.pc.common;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/7 13:19
 */
public enum ServerCmd {
    INIT_NPC(1001,"初始化小怪"),
    NPC_MOVE(1002,"小怪移动"),

    INIT_USER_OK(200, "玩家进入房间等待游戏"),

    INIT_USER_ROLE(201,"初始化用户角色，开始游戏"),

    USER_MOVE(202,"玩家移动命令"),

    USER_ROLE_SKILL(203,"玩家释放技能命令"),

    SKILL_MOVE(204,"技能正在移动命令"),
    SKILL_DELETE(205,"技能失效，移除界面命令"),
    USER_ATTACK(206,"用户普通攻击命令"),
    REFRESH_USER_INFO(207,"刷新用户信息， 血量，蓝条"),
    EXIT_GAME(208,"退出游戏，即退出房间"),

    USER_SLIDE(209,"用户滑行 "),
    SERVER_USER_SLIDE(210,"服务响应客户端，用户滑行 "),


    INIT_ROOM(400,"初始化房间，命令"),
    GET_ALL_ROOM(401,"获取所有邮箱游戏房间命令")
    ,
    GET_ONLINE_USER_SIZE(402, "获取在线用户数"),
    ONLINE_USER_SIZE(403, "返回在线用户数");


    public int value;
    public String msg;

    ServerCmd(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
