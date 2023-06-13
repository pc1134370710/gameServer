package com.pc.server.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.common.RpcProtocol;
import com.pc.common.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.RoomMsgData;
import com.pc.server.RpcNettyServer;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 服务器 处理获取所有房间
 * @author: pangcheng
 * @time: 2023/6/12 16:36
 */
public class GetAllRoomServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, Channel channel) {
        // 存储连接信息
        RpcNettyServer.channelMap.put(channel.id()+"",channel);

        List<RoomMsgData> list = new ArrayList<>();
        RpcNettyServer.roomServerMap.values().forEach(a->{
            RoomMsgData roomMsgData = new RoomMsgData();
            roomMsgData.setRoomId(a.getId());
            roomMsgData.setMaxUserSize(a.getMaxUserSize());
            roomMsgData.setUserSize(a.getUser().size());
            roomMsgData.setFullUser(a.getUser().size() == a.getMaxUserSize());
            roomMsgData.setStartGame(a.getIsOK().get());
            roomMsgData.setRoomName(a.getRoomName());
            list.add(roomMsgData);
        });
        Msg m = new Msg();
        m.setCmd(ServerCmd.INIT_ROOM.getValue());
        m.setData(JSON.toJSONString(list));
        channel.writeAndFlush(RpcProtocol.getRpcProtocol(m));
    }
}
