package com.pc.client.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.awt.*;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/2 13:11
 */
@Data
public abstract class BasicModel {
    /**
     * 游戏面板的画笔
     */
    @JSONField(serialize = false)
    protected Graphics2D gameGraphics2D;

    public abstract void paintOneself();


}
