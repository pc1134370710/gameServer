package com.pc.server.cmd.impl;

import com.pc.server.model.UserModel;
import com.pc.common.prtotcol.RpcProtocol;
import com.pc.common.prtotcol.ServerCmd;
import com.pc.server.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.cache.UserCache;

/**
 * @description: 获取在线人数
 * @author: pangcheng
 * @time: 2023/6/12 16:33
 */
public class OnlineUserServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, UserModel userModel) {
        Msg m = new Msg();
        m.setCmd(ServerCmd.GET_ONLINE_USER_SIZE.getValue());
        m.setData(UserCache.countRepositorySize().toString());
        // 由于是房间管道， 所以就直接返回了
        userModel.writeAndFlush(RpcProtocol.getRpcProtocol(m));
    }

}
