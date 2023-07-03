package com.pc.server.model;

import cn.hutool.core.lang.Assert;
import com.pc.common.constant.Constant;
import com.pc.common.msg.UserRoleMsgData;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/2 15:51
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class UserModel {

    /**
     * 是否已经准备就绪
     */
    private int start;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * channelId
     */
    private String channelId;

    /**
     * 是否已经死亡
     */
    private AtomicBoolean isDeath = new AtomicBoolean(false);

    private Channel channel;

    private UserRoleMsgData userRoleMsgData;

    public UserModel() {
        userRoleMsgData = new UserRoleMsgData().init();
    }

    /**
     * 解析 移动数据包
     */
    public void analysisMoveMsg(UserRoleMsgData userRoleMoveMsgData) {
        if (userRoleMoveMsgData.getUserX() != null) {
            this.getUserRoleMsgData().setUserX(userRoleMoveMsgData.getUserX());
        }
        if (userRoleMoveMsgData.getUserY() != null) {
            this.getUserRoleMsgData().setUserY(userRoleMoveMsgData.getUserY());
        }
        if (userRoleMoveMsgData.getDirection() != null) {
            this.getUserRoleMsgData().setDirection(userRoleMoveMsgData.getDirection());
        }
    }

    /**
     * 解析攻击 数据包
     */
    public void analysisAttackMsg(UserRoleMsgData userRoleMoveMsgData) {

        if (userRoleMoveMsgData.getAttack() != null) {
            this.getUserRoleMsgData().setAttack(userRoleMoveMsgData.getAttack());
        }
    }

    /**
     * 解析闪现技能 数据包
     */
    public void analysisSlideMsg(UserRoleMsgData userRoleMoveMsgData) {

        if (userRoleMoveMsgData.getSlide() != null) {
            this.getUserRoleMsgData().setSlide(userRoleMoveMsgData.getSlide());
            // 如果是滑行
            if (userRoleMoveMsgData.getSlide()) {
                int k = Constant.SELIDE_SEPEDD * this.getUserRoleMsgData().getDirection();
                this.getUserRoleMsgData().setUserX(this.getUserRoleMsgData().getUserX() + k);
            }
        }

    }

    /**
     * 服务器端只 解析坐标，是否释放技能， 普攻
     */
    public void analysisMsg(UserRoleMsgData userRoleMoveMsgData) {
        if (userRoleMoveMsgData.getUserX() != null) {
            this.getUserRoleMsgData().setUserX(userRoleMoveMsgData.getUserX());
        }
        if (userRoleMoveMsgData.getUserY() != null) {
            this.getUserRoleMsgData().setUserY(userRoleMoveMsgData.getUserY());
        }
        if (userRoleMoveMsgData.getAttack() != null) {
            this.getUserRoleMsgData().setAttack(userRoleMoveMsgData.getAttack());
        }
        if (userRoleMoveMsgData.getDirection() != null) {
            this.getUserRoleMsgData().setDirection(userRoleMoveMsgData.getDirection());
        }
        if (userRoleMoveMsgData.getSlide() != null) {
            this.getUserRoleMsgData().setSlide(userRoleMoveMsgData.getSlide());
            // 如果是滑行
            if (userRoleMoveMsgData.getSlide()) {
                int k = Constant.SELIDE_SEPEDD * this.getUserRoleMsgData().getDirection();
                this.getUserRoleMsgData().setUserX(this.getUserRoleMsgData().getUserX() + k);
            }
        }
    }

    public void close() {
        channel.close();
        // 放弃引用
        channel = null;
    }

    public void writeAndFlush(Object msg) {
        Assert.notNull(channel);
        channel.writeAndFlush(msg);
    }

}
