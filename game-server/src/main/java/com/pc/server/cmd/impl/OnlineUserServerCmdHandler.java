package com.pc.server.cmd.impl;

import com.pc.common.RpcProtocol;
import com.pc.common.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.server.RpcNettyServer;
import io.netty.channel.Channel;

/**
 * @description: 获取在线人数
 * @author: pangcheng
 * @time: 2023/6/12 16:33
 */
public class OnlineUserServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {
        Msg m = new Msg();
        m.setCmd(ServerCmd.GET_ONLINE_USER_SIZE.getValue());
        m.setData(RpcNettyServer.channelMap.size()+"");
        // 由于是房间管道， 所以就直接返回了
        channel.writeAndFlush(RpcProtocol.getRpcProtocol(m));
    }
}
