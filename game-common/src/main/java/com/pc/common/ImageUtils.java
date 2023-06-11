package com.pc.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/6 19:13
 */
public class ImageUtils {

    public static BufferedImage getImageFromResources(String imgName) {
        URL url = ImageUtils.class.getClassLoader().getResource("kongl" + imgName);
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static BufferedImage getImageFromResourcesLb(String imgName) {
        URL url = ImageUtils.class.getClassLoader().getResource("image" + imgName);
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        ImageUtils.getImageFromResources(Constant.gameImagePath);
    }
}
