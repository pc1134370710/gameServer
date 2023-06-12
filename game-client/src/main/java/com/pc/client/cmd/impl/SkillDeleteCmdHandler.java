package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.client.cmd.CmdHandler;
import com.pc.client.model.SkillModel;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomPanel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;

import java.util.Map;

/**
 * @description: 技能越界移除，或者技能失效
 * @author: pangcheng
 * @time: 2023/6/12 14:14
 */
public class SkillDeleteCmdHandler  implements CmdHandler {

    @Override
    public void doHandle(Msg msg, GamePanel gamePanel, RoomPanel roomPanel) {
        Map<String, SkillModel> stringSkillModelMap = LocalGameInfo.stringSkillModelMap;
        SkillMsgData skillMsgData = JSON.parseObject(msg.getData(), SkillMsgData.class);
        stringSkillModelMap.remove(skillMsgData.getSkillId());
    }
}
