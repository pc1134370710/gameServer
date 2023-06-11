package com.pc.common.msg;

import com.pc.common.Constant;
import lombok.Data;

import java.awt.*;
import java.util.UUID;

/**
 * @description: 技能消息包
 * @author: pangcheng
 * @create: 2023-06-03 12:19
 **/
@Data
public class SkillMsgData {

    /**
     * 技能id
     */
    private String skillId;

    /**
     * 技能释放者
     */
    private String userId;

    /**
     * 技能坐标
     */
    private int x;
    private int y;

    // 是否往左移动
    /**
     *  -1 往左， 1 往右
     */
    private Integer isLeftMove;
    //技能移动速度
    private int speed;

    public SkillMsgData() {
        this.skillId = UUID.randomUUID().toString();
        this.speed = Constant.SKillSpeed;
    }


    /**
     * 检测技能是否有效
     * @return
     */
    public boolean checkSkill(){
        if(x >= Constant.withe || x<=0){
            return false;
        }
        return true;
    }
    /**
     * 技能移动
     */
    public void move(){
        this.x += speed * isLeftMove;
    }

    public  Rectangle rectangle() {
        // 这里的x y 是
        return new Rectangle(this.x,this.y,65,75);
    }
}
