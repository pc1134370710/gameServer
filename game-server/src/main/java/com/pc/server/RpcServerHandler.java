/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pc.server;

import com.alibaba.fastjson.JSON;
import com.pc.common.RpcProtocol;
import com.pc.common.ServerCmd;
import com.pc.common.msg.Msg;
import com.pc.common.msg.RoomMsgData;
import com.pc.common.msg.SkillMsgData;
import com.pc.common.msg.UserRoleMsgData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;


public class RpcServerHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    /*
        handlerAdded() 当检测到新连接之后，调用 ch.pipeline().addLast(new XXXHandler()); 之后的回调
        channelRegistered() 当前的 channel 的所有的逻辑处理已经和某个 NIO 线程建立了绑定关系
        channelActive() channel 的 pipeline 中已经添加完所有的 handler，并且绑定好一个 NIO 线程之后，这条连接算是真正激活了，接下来就会回调到此方法。
        channelRead() 收到发来的数据，每次都会回调此方法，表示有数据可读。
        channelReadComplete() 数据读取完毕回调此方法
        channelInactive()  表示这条连接已经被关闭了，这条连接在 TCP 层面已经不再是 ESTABLISH 状态了
        channelUnregistered()  表示与这条连接对应的 NIO 线程移除掉对这条连接的处理
        handlerRemoved() 这条连接上添加的所有的业务逻辑处理器都被移除掉后调用
     */
    /**
     * 离线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("玩家离线，通道已经被关闭");
        RpcNettyServer.channelMap.remove(ctx.channel().id()+"");
    }

    /**
     * 接受消息
     * @param channelHandlerContext
     * @param rpcProtocol
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol) throws Exception {
        String json = new String(rpcProtocol.getContent());
        System.out.println("服务器收到消息："+ json);
        Msg msg = JSON.parseObject(json, Msg.class);

        // 退出游戏，退出房间
        if(msg.getCmd() == ServerCmd.EXIT_GAME.getValue()){
            Channel channel = channelHandlerContext.channel();
            RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
            // 移除房间内的用户
            roomServer.getUser().remove(msg.getUserId());

            // 告诉所有玩家 这个人走了， 移除改用户
            Msg msg1 = Msg.getMsg(ServerCmd.EXIT_GAME.getValue(), msg.getUserId(), null);
            roomServer.putMsg(msg1);

            return;

        }

        // 准备就绪
        if(msg.getCmd() == ServerCmd.INIT_USER_OK.getValue()){
            Channel channel = channelHandlerContext.channel();
            UserModel userModel = new UserModel();
            userModel.setUserId(msg.getUserId());
            userModel.setChannel(channel);
            userModel.setStart(1);
            // 建立房间跟 通道的关系
            // 将用户加入到房间中
            RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
            roomServer.getUser().put(userModel.getUserId(), userModel);

            // 刷新房间数据, todo 通知房间内的用户，当前进来多少人了
            return;
        }

        // 收到用户移动 todo 待修改成 服务器端移动用户
        if(msg.getCmd() == ServerCmd.USER_MOVE.getValue()){
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
            UserModel userModel = roomServer.getUser().get(userRoleMoveMsgData.getUserId());
            // 解析客户端的 操作数据包
            userModel.analysisMsg(userRoleMoveMsgData);
            // 通知其他客户端移动
            roomServer.putMsg(msg);
            return;

        }
        // 收到释放技能
        if(msg.getCmd() == ServerCmd.USER_ROLE_SKILL.getValue()){
            RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
            UserModel userModel = roomServer.getUser().get(msg.getUserId());
            // 存储技能
            SkillMsgData skillMsgData = JSON.parseObject(msg.getData(),SkillMsgData.class);
            // 设置技能方向坐标
            skillMsgData.setIsLeftMove(userModel.getUserRoleMsgData().getDirection());
            skillMsgData.setX(userModel.getUserRoleMsgData().getUserX());
            skillMsgData.setY(userModel.getUserRoleMsgData().getUserY());

            roomServer.getSkillMsgDataMap().put(skillMsgData.getSkillId(),skillMsgData);
            // 用户mp 归0， 发送刷新消息

            userModel.getUserRoleMsgData().setMp(userModel.getUserRoleMsgData().getMp()-50);
            Msg msg1 = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), userModel.getUserId(), userModel.getUserRoleMsgData());
            roomServer.putMsg(msg1);
            // 通知其他人，有人释放技能了
            msg.setData(JSON.toJSONString(skillMsgData));
            roomServer.putMsg(msg);
            return;
        }

        // 收到玩家滑行技能
        if(msg.getCmd() == ServerCmd.USER_FLASH.getValue()){
            RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
            // 存储技能
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            // 设置该用户的 攻击状态
            UserModel userModel = roomServer.getUser().get(userRoleMoveMsgData.getUserId());
            userModel.analysisMsg(userRoleMoveMsgData);
            // 蓝条归0
            userModel.getUserRoleMsgData().setMp(userModel.getUserRoleMsgData().getMp()-50);
            Msg msg1 = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), userModel.getUserId(), userModel.getUserRoleMsgData());
            roomServer.putMsg(msg1);

            Msg msg2 = Msg.getMsg(ServerCmd.USER_FLASH.getValue(), userModel.getUserId(), userModel.getUserRoleMsgData());
            // 通知其他人，玩家滑行技能
            roomServer.putMsg(msg2);
            return;
        }

        // 普通攻击
        if(msg.getCmd() == ServerCmd.USER_ATTACK.getValue()){
            RoomServer roomServer = RpcNettyServer.roomServerMap.get(msg.getRoomId());
            // 存储技能
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            // 设置该用户的 攻击状态
            UserModel userModel = roomServer.getUser().get(userRoleMoveMsgData.getUserId());
            userModel.analysisMsg(userRoleMoveMsgData);
            if(userRoleMoveMsgData.getAttack()){
                // 如果是触发攻击， 房间会检测攻击是否生效
                roomServer.getAttackQueue().put(userModel.getUserRoleMsgData());
            }
            // 通知其他人，有人释放技能了
            roomServer.putMsg(msg);
            return;
        }

        //  获取在线人数
        if(msg.getCmd() == ServerCmd.GET_ONLINE_USER_SIZE.getValue()){{
            Msg m = new Msg();
            m.setCmd(ServerCmd.GET_ONLINE_USER_SIZE.getValue());
            m.setData(RpcNettyServer.channelMap.size()+"");
            channelHandlerContext.writeAndFlush(RpcProtocol.getRpcProtocol(m));
            return;
        }}

        // 获取房间信息
        if(msg.getCmd() == ServerCmd.GET_ALL_ROOM.getValue()){
            Channel channel = channelHandlerContext.channel();
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
                list.add(roomMsgData);
            });
            Msg m = new Msg();
            m.setCmd(ServerCmd.INIT_ROOM.getValue());
            m.setData(JSON.toJSONString(list));
            channelHandlerContext.writeAndFlush(RpcProtocol.getRpcProtocol(m));
            return;
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
