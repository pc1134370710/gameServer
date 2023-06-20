package com.pc.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/6 18:42
 */
public class PropertiesUtil {

    public static Properties properties;

    public static void load() {
        properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = PropertiesUtil.class.getResourceAsStream("/config.properties");
        // 使用properties对象加载输入流
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.get(key).toString();
    }

    public static Integer getInteger(String key) {
        return Integer.valueOf(String.valueOf(properties.get(key)));
    }

}
