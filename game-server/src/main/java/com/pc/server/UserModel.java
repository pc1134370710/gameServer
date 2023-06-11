package com.pc.server;

import com.pc.common.msg.UserRoleMsgData;
import io.netty.channel.Channel;
import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/2 15:51
 */
@Data
public class UserModel {


    /**
     * 是否已经准备就绪
     */
    private int start;

    private String userId;


    /**
     * 是否已经死亡
     */
    private AtomicBoolean isDeath = new AtomicBoolean(false);

    private Channel channel;


    private UserRoleMsgData userRoleMsgData;

    public UserModel(){
        userRoleMsgData = new UserRoleMsgData().init();
    }

    /**
     * 服务器端只 解析坐标，是否释放技能， 普攻
     * @param userRoleMoveMsgData
     */
    public void analysisMsg(UserRoleMsgData userRoleMoveMsgData){

        if(userRoleMoveMsgData.getUserX()!=null){
            this.getUserRoleMsgData().setUserX(userRoleMoveMsgData.getUserX());
        }
        if(userRoleMoveMsgData.getUserY()!=null){
            this.getUserRoleMsgData().setUserY(userRoleMoveMsgData.getUserY());
        }

        if(userRoleMoveMsgData.getAttack()!=null){
            this.getUserRoleMsgData().setAttack(userRoleMoveMsgData.getAttack());
        }

        if(userRoleMoveMsgData.getDirection()!=null){
            this.getUserRoleMsgData().setDirection(userRoleMoveMsgData.getDirection());
        }
    }



}
