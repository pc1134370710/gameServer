package com.pc.client.cmd.impl;

import com.alibaba.fastjson.JSON;
import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.client.model.SkillModel;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;

import java.util.Map;

/**
 * @description: 玩家 大招释放技能
 * @author: pangcheng
 * @time: 2023/6/12 13:56
 */
public class UserSkillCmdHandler implements CmdHandler {


    @Override
    public void doHandle(Msg msg) {
        Map<String, SkillModel> stringSkillModelMap = LocalGameInfo.stringSkillModelMap;
        SkillMsgData skillMsgData = JSON.parseObject(msg.getData(), SkillMsgData.class);
        SkillModel skillModel = stringSkillModelMap.getOrDefault(skillMsgData.getSkillId(),new SkillModel());
        skillModel.analysisMsg(skillMsgData);
        // 游戏面板中添加 该游戏技能
        LocalGameInfo.gamePanel.addSkill(skillModel);
    }
}
