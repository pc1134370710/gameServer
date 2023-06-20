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

package com.pc.server.handler;

import com.alibaba.fastjson.JSON;
import com.pc.common.netty.model.UserModel;
import com.pc.common.prtotcol.RpcProtocol;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.common.msg.Msg;
import com.pc.common.netty.cache.UserCache;
import com.pc.server.cmd.ServerCmdHandleFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcProtocol> {
    private static final Logger log = LogManager.getLogger(RpcServerHandler.class);

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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserModel userModel = UserCache.createChannel(new UserModel()
                .setChannelId(ctx.channel().id().asShortText())
                .setChannel(ctx.channel()));
        log.info("用户连接 : {}", userModel);
        super.channelActive(ctx);
    }

    /**
     * 离线
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UserModel userModel = UserCache.getChannelInfo(ctx.channel().id().asShortText());
        if (null != userModel) {
            log.info("客户端 {} 离线", userModel);
        }
        UserCache.invalidate(ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }

    /**
     * 接受消息
     *
     * @param ctx
     * @param rpcProtocol
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol rpcProtocol) throws Exception {
        UserModel userModel = UserCache.getChannelInfo(ctx.channel().id().asShortText());
        String json = new String(rpcProtocol.getContent());
        Msg msg = JSON.parseObject(json, Msg.class);
        log.info("收到消息 {} ", msg);
        ServerCmdHandler cmdHandle = ServerCmdHandleFactory.getCmdHandle(msg.getCmd());
        cmdHandle.doHandle(msg, userModel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接异常
            ctx.close();
        } else {
            log.warn("发生异常", cause);
            super.exceptionCaught(ctx, cause);
        }
    }

}
