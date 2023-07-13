package com.pc.common.msg;

import lombok.Data;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/7/13 14:04
 */
@Data
public class UserMoveMsgData {
    /**
     *  上下左右按键
     */
    private Integer up;
    private Integer down;
    private Integer left;
    private Integer right;

}
