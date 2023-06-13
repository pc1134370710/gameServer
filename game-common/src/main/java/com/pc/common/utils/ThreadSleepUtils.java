package com.pc.common.utils;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/13 17:32
 */
public class ThreadSleepUtils {

    private ThreadSleepUtils(){

    }

    public static void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
