package com.pc.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pc.client.cache.LocalGameInfo;
import com.pc.client.model.NpcMonster;
import com.pc.client.model.SkillModel;
import com.pc.client.model.UserRoleModel;
import com.pc.client.model.gui.GamePanel;
import com.pc.client.model.gui.RoomButton;
import com.pc.client.model.gui.RoomPanel;
import com.pc.common.RpcProtocol;
import com.pc.common.ServerCmd;
import com.pc.common.msg.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Map;

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

    /**
     * 游戏面板
     */
    private GamePanel gamePanel;
    private RoomPanel roomPanel;



    public RpcClientSyncHandler( GamePanel gamePanel,RoomPanel roomPanel){
        this.gamePanel= gamePanel;
        this.roomPanel= roomPanel;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol) {
        String json = new String(rpcProtocol.getContent());
        System.out.println("收到消息："+json);
        Msg msg = JSON.parseObject(json, Msg.class);

        // 收到初始化npc 消息
        if(msg.getCmd() == ServerCmd.INIT_NPC.getValue()){
            List<MonsterMsgData> npcMonster = JSON.parseObject(msg.getData(), new TypeReference< List<MonsterMsgData>>(){});
            for(MonsterMsgData monster : npcMonster){
                NpcMonster npcMonster1 = new NpcMonster();
                npcMonster1.analysisMsg( monster);
                gamePanel.addMonsters(npcMonster1);
            }
            return;
        }
        // 接收 npc 移动规则
        if(msg.getCmd() == ServerCmd.NPC_MOVE.getValue()){
            Map<String, NpcMonster> npcMonsters = LocalGameInfo.npcMonsters;
            MonsterMsgData npcMonster = JSON.parseObject(msg.getData(), MonsterMsgData.class);
            NpcMonster npcMonster1 = npcMonsters.get(npcMonster.getMonsterId());
            npcMonster1.analysisMsg(npcMonster);
            return;
        }

        // 初始化用户角色
        if(msg.getCmd() == ServerCmd.INIT_USER_ROLE.getValue()){
            Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            UserRoleModel userRoleModel = userRoleModelMap.getOrDefault(userRoleMoveMsgData.getUserId(),new UserRoleModel());
            userRoleModel.analysisMsg(userRoleMoveMsgData);
            gamePanel.addUser(userRoleModel);
            return;
        }

        // 收到移动角色消息
        if(msg.getCmd() == ServerCmd.USER_MOVE.getValue()){
            Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
            userRoleModel.analysisMsg(userRoleMoveMsgData);
            return;
        }
        // 用户普攻
        if(msg.getCmd() == ServerCmd.USER_ATTACK.getValue()){
            Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
            userRoleModel.analysisMsg(userRoleMoveMsgData);
            return;
        }
        // 刷新用户蓝条，血量
        if(msg.getCmd() == ServerCmd.REFRESH_USER_INFO.getValue()){
            Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
            userRoleModel.analysisMsg(userRoleMoveMsgData);
            return;
        }


        // 收到放技能的消息
        if(msg.getCmd() ==ServerCmd.USER_ROLE_SKILL.getValue()){
            Map<String, SkillModel> stringSkillModelMap = LocalGameInfo.stringSkillModelMap;
            SkillMsgData skillMsgData = JSON.parseObject(msg.getData(), SkillMsgData.class);
            SkillModel skillModel = stringSkillModelMap.getOrDefault(skillMsgData.getSkillId(),new SkillModel());
            skillModel.analysisMsg(skillMsgData);
            gamePanel.addSkill(skillModel);
            return;
        }
        // 收到退出游戏
        if(msg.getCmd() == ServerCmd.EXIT_GAME.getValue()){
            // 移除该用户
            LocalGameInfo.userRoleModelMap.remove(msg.getUserId());
            return;
        }

        // 收到技能移动
        if(msg.getCmd() == ServerCmd.SKILL_MOVE.getValue()){
            Map<String, SkillModel> stringSkillModelMap = LocalGameInfo.stringSkillModelMap;
            SkillMsgData skillMsgData = JSON.parseObject(msg.getData(), SkillMsgData.class);
            SkillModel skillModel = stringSkillModelMap.get(skillMsgData.getSkillId());
            skillModel.analysisMsg(skillMsgData);
            return;
        }
        // 收到技能失效 ，移除边界
        if(msg.getCmd() == ServerCmd.SKILL_DELETE.getValue()){
            Map<String, SkillModel> stringSkillModelMap = LocalGameInfo.stringSkillModelMap;
            SkillMsgData skillMsgData = JSON.parseObject(msg.getData(), SkillMsgData.class);
            stringSkillModelMap.remove(skillMsgData.getSkillId());
            return;
        }
        // 用户释放滑行技能
        if(msg.getCmd() == ServerCmd.SERVER_USER_SLIDE.getValue()){
            Map<String, UserRoleModel> userRoleModelMap = LocalGameInfo.userRoleModelMap;
            UserRoleMsgData userRoleMoveMsgData = JSON.parseObject(msg.getData(), UserRoleMsgData.class);
            UserRoleModel userRoleModel = userRoleModelMap.get(userRoleMoveMsgData.getUserId());
            userRoleModel.analysisMsg(userRoleMoveMsgData);
            return;
        }

        // 初始化房间信息
        if(msg.getCmd() == ServerCmd.INIT_ROOM.getValue()){
            List<RoomMsgData> roomMsgDatas = JSON.parseObject(msg.getData(), new TypeReference< List<RoomMsgData>>(){});
            roomPanel.removeAll();
            roomMsgDatas.forEach(a->{
                RoomButton button = new RoomButton(a.getRoomId(),a.getMaxUserSize(),a.getUserSize(),a.getFullUser(),a.getStartGame());
                roomPanel.add(button);
            });
            // 添加组件后,需要调用validate()通知布局管理器组件已添加, repaint()重绘容器。如果不调用,组件不会显示
            roomPanel.validate();
            roomPanel.repaint();
            return;
        }
        // 收到获取在线用户数
        if(msg.getCmd() == ServerCmd.ONLINE_USER_SIZE.getValue()){{
            LocalGameInfo.roomJFrame.setTitle("当前玩家：" + LocalGameInfo.userId+", 当前在线用户数："+msg.getData());
            LocalGameInfo.gameFrame.setTitle("当前玩家：" + LocalGameInfo.userId+", 当前在线用户数："+msg.getData());
            return;
        }}

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
