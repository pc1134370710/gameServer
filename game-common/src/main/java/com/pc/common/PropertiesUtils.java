package com.pc.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/6 18:42
 */
public class PropertiesUtils {

    public static Properties properties;

    public static void load() {
         properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
//        getClass().getResourceAsStream("/" + ApiConsts.LOCALPROPERTIES)
        InputStream in = PropertiesUtils.class.getResourceAsStream("/config.properties");
        // 使用properties对象加载输入流

        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key){
        System.out.println(properties.get(key)+"  "+key);
        return  properties.get(key) +"";
    }
    public static Integer getInteger(String key){
        return Integer.valueOf( properties.get(key)+"");
    }

}
