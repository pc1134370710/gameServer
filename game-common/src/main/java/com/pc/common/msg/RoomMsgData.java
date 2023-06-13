package com.pc.common.msg;

import lombok.Data;

/**
 * @description: 房间信息 数据包
 * @author: pangcheng
 * @create: 2023-06-03 14:20
 **/
@Data
public class RoomMsgData {

    /**
     * 房间id
     */
    private String roomId;

    /**
     * 房间最大用户数量
     */
    private Integer maxUserSize;

    /**
     * 当前用户人数
     */
    private Integer userSize;

    /**
     * 是否已经满人了
     */
    private Boolean fullUser;
    /**
     * 开始游戏
     */
    private Boolean startGame;

    private String roomName;

}
