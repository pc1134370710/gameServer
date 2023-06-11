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

import com.pc.common.RpcDecoder;
import com.pc.common.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://www.cnblogs.com/nanaheidebk/p/11025362.html
 * Netty Server 启动类
 */
public class RpcNettyServer {

    /**
     *  所有的获取房间的连接信息
     *  key, 管道id -- 获取房间信息的管道
     */
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    /**
     * 房间
     */
    public static Map<String, RoomServer> roomServerMap = new ConcurrentHashMap<>();
    /*
     * bossGroup 和 workerGroup 是两个线程池, 它们默认线程数为 CPU 核心数乘以 2，
     * bossGroup 用于接收客户端传过来的请求，接收到请求后将后续操作交由 workerGroup 处理。
     */
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private int port;

    public RpcNettyServer(int port) {
        this.port = port;
        // 初始化房间
        roomServerMap.put("1",new RoomServer("1",2));
        roomServerMap.put("2",new RoomServer("2",1));
        roomServerMap.put("3",new RoomServer("3",6));

    }

    public void run() throws Exception {
        /*
            但是，在 Netty 的服务器端的 acceptor 阶段，没有使用到多线程, 因此上面的主从多线程模型在 Netty 的实现是有误的。
            服务器端的 ServerSocketChannel 只绑定到了 bossGroup 中的一个线程，因此在调用 Java NIO
            的 Selector.select 处理客户端的连接请求时，实际上是在一个线程中的，
            所以对只有一个服务的应用来说，bossGroup 设置多个线程是没有什么作用的，反而还会造成资源浪费。
         */
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .childOption(ChannelOption.SO_SNDBUF,512)//设置发送缓冲区
                .childOption(ChannelOption.SO_RCVBUF,1024*3)//设置发送缓冲区
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        /*
                            用于处理或截获通道的接收和发送数据。它提供了一种高级的截取过滤模式（类似serverlet中的filter功能）
                            通过 addLast 方法将一个一个的 ChannelHandler 添加到责任链上并给它们取个名称（不取也可以，Netty 会给它个默认名称），
                            这样就形成了链式结构。在请求进来或者响应出去时都会经过链上这些 ChannelHandler 的处理。
                         */
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("Encoder", new RpcEncoder());
                        pipeline.addLast("Decoder", new RpcDecoder());
                        pipeline.addLast("Handler", new RpcServerHandler());

                    }
                });

        //绑定监听端口，调用sync同步阻塞方法等待绑定操作完成，完成后返回ChannelFuture类似于JDK中Future
        Channel channel = serverBootstrap.bind(port).sync().channel();
        //等待服务端口关闭
        channel.closeFuture().sync();
        System.out.println("服务器启动完毕！");
    }

    public static void main(String[] args)  {
        RpcNettyServer rpcNettyServer = new RpcNettyServer(10096);
        try {
            rpcNettyServer.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("服务器启动完毕！");
    }
}
