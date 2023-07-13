package com.pc.client.model;

import com.pc.common.constant.Constant;
import com.pc.common.util.ImageUtil;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @description: 游戏背景
 * @author: pangcheng
 * @time: 2023/6/2 10:05
 */
@Data
public class GameBackdrop  extends BasicModel {

    /**
     * 图片背景
     */
    private BufferedImage image;


    public GameBackdrop(Graphics2D gameGraphics2D){

        this.gameGraphics2D = gameGraphics2D;
        // 将背景图画 到画板上
        try {// 读取图片

            this.image = ImageUtil.getImageFromResourcesLb(Constant.gameImagePath);
        } catch (Exception e) {
            System.out.println("加载游戏背景失败");
            e.printStackTrace();
        }
    }

    public void  start(){
        this.gameGraphics2D.drawImage(image,0,0,null);
    }

    @Override
    public void paintOneself() {
        start();
    }

}
