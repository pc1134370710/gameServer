package com.pc.client.cmd.impl;

import com.pc.client.cache.LocalGameInfo;
import com.pc.common.cmd.CmdHandler;
import com.pc.common.msg.Msg;

/**
 * @description:  聊天消息处理器
 * @author: pangcheng
 * @create: 2023-06-17 13:39
 **/
public class ChatMsgCmdHandler  implements CmdHandler {

    @Override
    public void doHandle(Msg msg) {
        LocalGameInfo.chatPanel.addText(msg.getData().substring(1,msg.getData().length()-1));
        LocalGameInfo.chatPanel.validate();
        LocalGameInfo.chatPanel.repaint();
    }
}
