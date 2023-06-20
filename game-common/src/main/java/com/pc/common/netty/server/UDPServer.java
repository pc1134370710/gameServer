package com.pc.common.netty.server;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public abstract class UDPServer extends Server {

    protected UDPServer(String tag, int port, int workerCore) {
        super(tag, port, workerCore);
    }

    protected AbstractBootstrap initialize() {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(tag, Thread.MAX_PRIORITY));
        return new Bootstrap()
                .group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(NioChannelOption.SO_REUSEADDR, true)
                .option(NioChannelOption.SO_RCVBUF, 1024 * 1024 * 50)
                .handler(getChannelInitializer());
    }

    public abstract ChannelInitializer<NioDatagramChannel> getChannelInitializer();

}
