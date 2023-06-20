package com.pc.server.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.common.netty.cache.RoomCache;
import com.pc.common.netty.model.UserModel;
import com.pc.common.prtotcol.RpcProtocol;
import com.pc.common.prtotcol.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.msg.RoomMsgData;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 服务器 处理获取所有房间
 * @author: pangcheng
 * @time: 2023/6/12 16:36
 */
public class GetAllRoomServerCmdHandler implements ServerCmdHandler {

    @Override
    public void doHandle(Msg msg, UserModel userModel) {
        List<RoomMsgData> list = new ArrayList<>();
        RoomCache.all().asMap().values().forEach(a -> {
            RoomMsgData roomMsgData = new RoomMsgData();
            roomMsgData.setRoomId(a.getId());
            roomMsgData.setMaxUserSize(a.getMaxUserSize());
            roomMsgData.setUserSize((int) a.getUser().estimatedSize());
            roomMsgData.setFullUser((int) a.getUser().estimatedSize() == a.getMaxUserSize());
            roomMsgData.setStartGame(a.getIsOK().get());
            roomMsgData.setRoomName(a.getRoomName());
            list.add(roomMsgData);
        });
        Msg m = new Msg();
        m.setCmd(ServerCmd.INIT_ROOM.getValue());
        m.setData(JSON.toJSONString(list));
        userModel.writeAndFlush(RpcProtocol.getRpcProtocol(m));
    }

}
