package com.pc.client;

import com.alibaba.fastjson.JSON;
import com.pc.common.cmd.CmdHandler;
import com.pc.client.cmd.ClientCmdHandleFactory;
import com.pc.common.RpcProtocol;
import com.pc.common.msg.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;

/**
 * 这里使用并发的等待-通知机制来拿到结果
 *
 * SimpleChannelInboundHandler  不需要手动释放内存
 *
 *ChannelInboundHandlerAdapter 需要手动释放
 *  ReferenceCountUtil.release(msg);
 *
 */
public class RpcClientSyncHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    private Logger log = LogManager.getLogger(RpcClientSyncHandler.class);


    public RpcClientSyncHandler(){


    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol) throws UnsupportedEncodingException {
        String json = new String(rpcProtocol.getContent(),"utf-8");
        System.out.println("收到消息："+json);
        Msg msg = JSON.parseObject(json, Msg.class);

//        // 收到初始化npc 消息
//        if(msg.getCmd() == ServerCmd.INIT_NPC.getValue()){
//            List<MonsterMsgData> npcMonster = JSON.parseObject(msg.getData(), new TypeReference< List<MonsterMsgData>>(){});
//            for(MonsterMsgData monster : npcMonster){
//                NpcMonster npcMonster1 = new NpcMonster();
//                npcMonster1.analysisMsg( monster);
//                gamePanel.addMonsters(npcMonster1);
//            }
//            return;
//        }
//        // 接收 npc 移动规则
//        if(msg.getCmd() == ServerCmd.NPC_MOVE.getValue()){
//            Map<String, NpcMonster> npcMonsters = LocalGameInfo.npcMonsters;
//            MonsterMsgData npcMonster = JSON.parseObject(msg.getData(), MonsterMsgData.class);
//            NpcMonster npcMonster1 = npcMonsters.get(npcMonster.getMonsterId());
//            npcMonster1.analysisMsg(npcMonster);
//            return;
//        }
        // 根据命令字 获取执行器
        CmdHandler cmdHandle = ClientCmdHandleFactory.getCmdHandle(msg.getCmd());
        if(cmdHandle == null){
            log.warn("无效命令字, {}",msg.getCmd());
            return;
        }
        cmdHandle.doHandle(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
