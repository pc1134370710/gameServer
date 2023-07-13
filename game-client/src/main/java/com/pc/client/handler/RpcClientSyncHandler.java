package com.pc.client.handler;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.client.cmd.ClientCmdHandleFactory;
import com.pc.common.prtotcol.RpcProtocol;
import com.pc.common.msg.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 这里使用并发的等待-通知机制来拿到结果
 * <p>
 * SimpleChannelInboundHandler  不需要手动释放内存
 * <p>
 * ChannelInboundHandlerAdapter 需要手动释放
 * ReferenceCountUtil.release(msg);
 */
public class RpcClientSyncHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    private static final Logger log = LogManager.getLogger(RpcClientSyncHandler.class);

    public RpcClientSyncHandler() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol) {
        String json = new String(rpcProtocol.getContent(), StandardCharsets.UTF_8);
        log.info("收到消息：{}", json);
        Msg msg = JSON.parseObject(json, Msg.class);

        // 根据命令字 获取执行器
        CmdHandler cmdHandle = ClientCmdHandleFactory.getCmdHandle(msg.getCmd());
        if (cmdHandle == null) {
            log.warn("无效命令字, {}", msg.getCmd());
            return;
        }
        cmdHandle.doHandle(msg);
//        LocalGameInfo.gamePanel.repaint();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接异常
            ctx.close();
        } else {
            log.warn("发生异常", cause);
        }
    }

}
