package com.pc.common.msg;

import com.pc.common.Constant;
import lombok.Data;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 用户角色数据包
 * @author: pangcheng
 * @create: 2023-06-03 10:49
 **/
@Data
public class UserRoleMsgData {
    /**
     * 坐标
     */
    private Integer userX;
    private Integer userY;
    /**
     * id
     */
    private String userId;
    /**
     * 运动速度
     */
    private Integer moveSpeed;

    /**
     * 用户血量
     */
    private Integer hp;
    /**
     * 魔力值，满了可以释放技能
     */
    private Integer mp;

    /**
     * 发动普通攻击
     */
    private Boolean attack;

    /**
     * -1 往左， 1往右
     */
    private Integer direction;

    /**
     * 是否已经死亡
     */
    private Boolean isOver;
    /**
     * 是否滑行
     */
    private Boolean slide;

    public UserRoleMsgData() {

        // 默认都是 正常状态,因为会自动恢复，所以放在这

        this.moveSpeed = Constant.userRoleSpeed;
    }

    /**
     * 初始化用户坐标， 血量等
     * @return
     */
    public UserRoleMsgData init(){
        this.userX = Constant.withe / 2;
        this.userY = Constant.height / 2;
        this.hp = Constant.userRoleHP;
        this.mp = Constant.userRoleMP;
        this.direction = 1;
        this.isOver = false;
        this.slide = false;
        return this;
    }


    /**
     * 限制坐标
     */
    public void limitingXY() {
        // 判断边界
        if (this.userX >= Constant.withe) {
            this.userX = Constant.withe;
        }
        if (this.userY >= Constant.height) {
            this.userY = Constant.height;
        }
        // 坐标轴 0，0 在左上角
        if (this.userX <= 0) {
            this.userX = 0;
        }

        if (this.userY <= 0) {
            this.userY = 0;
        }
    }

    public Boolean getAttack() {
        return attack;
    }

    public Rectangle rectangle() {
        if(attack!=null && !attack){
            return new Rectangle(userX,userY-8,45,130);
        }
        else{
            if(direction>0){
                // 往右边
                return new Rectangle(userX-35,userY-8,200,130);
            }else{
                // 往左边
                return new Rectangle(userX-112,userY-8,200,130);
            }
        }
    }

}
