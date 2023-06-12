package com.pc.client.cmd;
import com.pc.client.cmd.impl.*;
import com.pc.common.ServerCmd;
import com.pc.common.cmd.CmdHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:  命令字处理器工厂
 * @author: pangcheng
 * @time: 2023/6/12 13:41
 */
public class ClientCmdHandleFactory {


    private static Map<Integer, CmdHandler> cmdHandleMap = new HashMap<>();


    static {
        cmdHandleMap.put(ServerCmd.START_GAME.getValue(), new StartGameCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_MOVE.getValue(), new UserMoveCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_ROLE_SKILL.getValue(), new UserSkillCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_ATTACK.getValue(), new UserAttackCmdHandler());
        cmdHandleMap.put(ServerCmd.REFRESH_USER_INFO.getValue(), new RefreshUserInfoCmdHandler());
        cmdHandleMap.put(ServerCmd.SKILL_MOVE.getValue(), new SkillMoveCmdHandler());
        cmdHandleMap.put(ServerCmd.SKILL_DELETE.getValue(), new SkillDeleteCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_FLASH.getValue(), new UserFlashCmdHandler());
        cmdHandleMap.put(ServerCmd.INIT_ROOM.getValue(), new InitRoomCmdHandler());
        cmdHandleMap.put(ServerCmd.GET_ONLINE_USER_SIZE.getValue(), new OnlineUserCmdHandler());
        cmdHandleMap.put(ServerCmd.EXIT_GAME.getValue(), new ExitGameCmdHandler());

    }
    private ClientCmdHandleFactory(){

    }
    /**
     * 获取命令字处理器
     * @param cmd
     * @return
     */
    public static CmdHandler getCmdHandle(Integer cmd){
        return cmdHandleMap.get(cmd);
    }


}
