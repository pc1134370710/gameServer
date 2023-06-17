//package com.pc.client.utils;
//
///**
// * @description:
// * @author: pangcheng
// * @create: 2023-06-17 11:56
// **/
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class RateLimiter {
//    private AtomicInteger semaphore;
//    private long timeInterval;
//
//    public RateLimiter(int permits, long timeInterval) {
//        this.semaphore = new AtomicInteger(permits);
//        this.timeInterval = timeInterval;
//    }
//
//    public boolean doOperation() {
//        try {
//            if(semaphore.get()>0){
//                return true;
//            }else{
//                return false;
//            }
//            semaphore.decrementAndGet(); // 请求信号量
//
//            // 执行你的操作
////            System.out.println("Operation performed!");
//
//            Thread.sleep(timeInterval); // 模拟操作的时间
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            semaphore.release(); // 释放信号量
//        }
//    }
//
//    public static void main(String[] args) {
//        int permits = 5; // 允许的操作次数
//        long timeInterval = 1000; // 时间间隔（毫秒）
//
//        RateLimiter rateLimiter = new RateLimiter(permits, timeInterval);
//
//        // 模拟连续操作
//        for (int i = 0; i < 10; i++) {
//            rateLimiter.doOperation();
//            System.out.println("dddddddddd");
//
//            try {
//                Thread.sleep(200); // 间隔200毫秒
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
//
