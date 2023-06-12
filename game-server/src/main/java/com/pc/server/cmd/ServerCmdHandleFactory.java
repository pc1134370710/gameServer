package com.pc.server.cmd;
import com.pc.common.ServerCmd;
import com.pc.common.cmd.ServerCmdHandler;
import com.pc.server.cmd.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:  命令字处理器工厂
 * @author: pangcheng
 * @time: 2023/6/12 13:41
 */
public class ServerCmdHandleFactory {


    private static Map<Integer, ServerCmdHandler> cmdHandleMap = new HashMap<>();


    static {
        cmdHandleMap.put(ServerCmd.USER_MOVE.getValue(), new UserMoveServerCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_ROLE_SKILL.getValue(), new UserSkillServerCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_ATTACK.getValue(), new UserAttackServerCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_FLASH.getValue(), new UserFlashServerCmdHandler());
        cmdHandleMap.put(ServerCmd.GET_ALL_ROOM.getValue(), new GetAllRoomServerCmdHandler());
        cmdHandleMap.put(ServerCmd.GET_ONLINE_USER_SIZE.getValue(), new OnlineUserServerCmdHandler());
        cmdHandleMap.put(ServerCmd.EXIT_GAME.getValue(), new ExitGameServerCmdHandler());
        cmdHandleMap.put(ServerCmd.USER_INTO_ROOM.getValue(),new UserIntoRoomCmdHandler());

    }
    private ServerCmdHandleFactory(){

    }
    /**
     * 获取命令字处理器
     * @param cmd
     * @return
     */
    public static ServerCmdHandler getCmdHandle(Integer cmd){
        return cmdHandleMap.get(cmd);
    }


}