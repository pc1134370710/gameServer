package com.pc.common.netty.server;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public abstract class TCPServer extends Server {

    protected TCPServer(String tag, int port, int workerCore) {
        super(tag, port, workerCore);
    }

    protected AbstractBootstrap initialize() {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(tag, Thread.MAX_PRIORITY));
        workerGroup = new NioEventLoopGroup(workerCore, new DefaultThreadFactory(tag, Thread.MAX_PRIORITY));
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_SNDBUF, 512)
                .childOption(ChannelOption.SO_RCVBUF, 1024 * 3)
                .childHandler(getChannelInitializer());
    }

    public abstract ChannelInitializer<SocketChannel> getChannelInitializer();

}