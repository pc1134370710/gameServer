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

import com.pc.common.constant.Constant;
import com.pc.common.netty.cache.RoomCache;
import com.pc.common.netty.server.TCPServer;
import com.pc.common.prtotcol.RpcDecoder;
import com.pc.common.prtotcol.RpcEncoder;
import com.pc.common.util.PropertiesUtil;
import com.pc.common.netty.model.RoomServer;
import com.pc.server.handler.RpcServerHandler;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Objects;

/**
 * Netty Server 启动类
 */
public class RpcNettyServer extends TCPServer {

    public RpcNettyServer(int port) {
        super("game-server", port, 2);
        this.port = port;
        // 初始化房间
        RoomCache.put("1", new RoomServer("1", 1, true, "单人电脑对战"));
        RoomCache.put("2", new RoomServer("2", 2, false, "多人混战"));
        RoomCache.put("3", new RoomServer("3", 2, true, "多人混战（包含电脑）"));
    }

    @Override
    public ChannelInitializer<SocketChannel> getChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();

                // TODO SSL/TLS 安全通道双向认证

                pipeline.addLast("encoder", new RpcEncoder());
                pipeline.addLast("decoder", new RpcDecoder());
                pipeline.addLast("handler", new RpcServerHandler());
            }
        };
    }

    public static void main(String[] args) {
        try {
            // 必须放在最前面初始化 log4j 的地址，该方式可以加载 jar 包同级目录下的文件
            ConfigurationSource source = new ConfigurationSource(Objects.requireNonNull(RpcNettyServer.class.getResourceAsStream("/log4j2.xml")));
            Configurator.initialize((ClassLoader) null, source);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PropertiesUtil.load();
        RpcNettyServer rpcNettyServer = new RpcNettyServer(PropertiesUtil.getInteger(Constant.PORT));
        rpcNettyServer.start();
    }

}
