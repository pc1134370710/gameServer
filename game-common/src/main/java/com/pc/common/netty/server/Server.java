package com.pc.common.netty.server;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

public abstract class Server {

    private static final Logger log = LogManager.getLogger(Server.class);

    protected boolean isRunning;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;
    protected ExecutorService businessGroup;
    protected String tag;
    protected int port;
    protected int workerCore;

    protected Server(String tag, int port, int workerCore) {
        this.tag = tag;
        this.port = port;
        this.workerCore = workerCore;
    }

    protected abstract AbstractBootstrap initialize();

    public synchronized boolean start() {
        if (isRunning) {
            log.warn("==={}已经启动,port:{}===", tag, port);
            return isRunning;
        }

        AbstractBootstrap bootstrap = initialize();
        ChannelFuture future = bootstrap.bind(port).awaitUninterruptibly();
        future.channel().closeFuture().addListener(f -> {
            if (isRunning) stop();
        });
        if (future.cause() != null)
            log.error("启动失败", future.cause());

        if (isRunning == future.isSuccess())
            log.warn("==={}启动成功,port:{}===", tag, port);
        return isRunning;
    }

    public synchronized void stop() {
        isRunning = false;
        bossGroup.shutdownGracefully();
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
        if (businessGroup != null)
            businessGroup.shutdown();
        log.warn("==={}已经停止,port:{}===", tag, port);
    }

}
