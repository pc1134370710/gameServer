package com.pc.common.msg;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/2 14:56
 */
@Data
public class Msg {
    /**
     *  指令类型
     */
    private int cmd;
    /**
     * 房间id
     *  房间内的游戏数据， 必传
     */
    private String roomId;
    /**
     *  用户名称 必传
     */
    private String userId;

    /**
     * 内容
     */
    private String data;
    /**
     * 房间人数 和最大人数
     */
    private int roomUserSize;
    private int roomUserMaxSize;
    public static  Msg getMsg(int cmd,String userId,Object data){
        Msg msg = new Msg();
        msg.setCmd(cmd);
        msg.setUserId(userId);
        if(data!=null){
            msg.setData(JSON.toJSONString(data));
        }
        return msg;
    }


}
