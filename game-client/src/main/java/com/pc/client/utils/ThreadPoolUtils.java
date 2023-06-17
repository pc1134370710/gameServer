package com.pc.client.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: pangcheng
 * @create: 2023-06-17 11:41
 **/
public class ThreadPoolUtils {


    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,20,60
            , TimeUnit.SECONDS,new ArrayBlockingQueue<>(1024));


}
