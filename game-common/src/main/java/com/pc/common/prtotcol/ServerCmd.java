package com.pc.common.prtotcol;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/7 13:19
 */
public enum ServerCmd {
    INIT_NPC(1001,"初始化电脑玩家"),
    NPC_MOVE(1002,"电脑玩家移动位置"),

    USER_INTO_ROOM(200, "玩家进入房间等待游戏"),

    START_GAME(201,"初始化用户角色，开始游戏"),

    USER_MOVE(202,"玩家移动命令"),

    USER_ROLE_SKILL(203,"玩家释放技能命令"),

    SKILL_MOVE(204,"技能正在移动命令"),
    SKILL_DELETE(205,"技能失效，移除界面命令"),
    USER_ATTACK(206,"用户普通攻击命令"),
    REFRESH_USER_INFO(207,"刷新用户信息， 血量，蓝条"),
    EXIT_GAME(208,"退出游戏，即退出房间"),

    USER_FLASH(209,"用户闪现 "),


    INIT_ROOM(400,"初始化房间，命令"),
    GET_ALL_ROOM(401,"获取所有邮箱游戏房间命令")
    ,
    GET_ONLINE_USER_SIZE(402, "获取在线用户数"),

    REGISTER_TASK(700, "注册聊天管道"),
    INIT_CHAT(701, "注册聊天管道"),
    CHAT_MSG(702, "发送聊天消息"),
    ;


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
