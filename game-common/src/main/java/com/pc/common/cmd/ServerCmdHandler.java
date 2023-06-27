package com.pc.common.cmd;

import com.pc.common.msg.Msg;
import com.pc.common.netty.model.UserModel;

/**
 * @description: 相关命令字处理器
 * @author: pangcheng
 * @time: 2023/6/12 13:41
 */
public interface ServerCmdHandler {

    /**
     * 处理动作
     *
     * @param msg 消息对象
     */
    void doHandle(Msg msg, UserModel userModel);

}
