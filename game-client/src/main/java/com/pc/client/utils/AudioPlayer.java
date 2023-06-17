package com.pc.client.utils;

/**
 * @description:
 * @author: pangcheng
 * @create: 2023-06-17 11:14
 **/
import com.pc.common.ImageUtils;
import com.pc.common.utils.ThreadSleepUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class AudioPlayer {

    private static void player(String path){

        ThreadPoolUtils.threadPoolExecutor.execute(()->{
            try {
                int k = 0;
                // 从文件中加载音频流
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));

                while (true){
                    if(k%3 == 0){
                        URL url = ImageUtils.class.getClassLoader().getResource("MP3/15279.wav"  );
                        audioInputStream = AudioSystem.getAudioInputStream(new File(URLDecoder.decode(url.getPath())));
                    }else{
                        URL url = ImageUtils.class.getClassLoader().getResource("MP3/3550.wav"  );
                        audioInputStream = AudioSystem.getAudioInputStream(new File(URLDecoder.decode(url.getPath())));
                    }

                    // 获取音频剪辑对象
                    Clip clip = AudioSystem.getClip();

                    // 打开音频剪辑并将音频流分配给剪辑
                    clip.open(audioInputStream);

                    // 开始播放音频
                    clip.start();

                    // 等待音频播放完毕
                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                }
                // 关闭音频剪辑和音频流
//                clip.close();
//                audioInputStream.close();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }

        });
    }


    public static void playerStartGame(){
        URL url = ImageUtils.class.getClassLoader().getResource("MP3/3550.wav"  );
        player(URLDecoder.decode(url.getPath()));

    }
    public static void playerAttack(){
        URL url = ImageUtils.class.getClassLoader().getResource("MP3/gj.wav"  );
        try {
            // 从文件中加载音频流
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(URLDecoder.decode(url.getPath())));
            // 获取音频剪辑对象
            Clip clip = AudioSystem.getClip();
            // 打开音频剪辑并将音频流分配给剪辑
            clip.open(audioInputStream);
            ThreadPoolUtils.threadPoolExecutor.execute(()->{
                // 开始播放音频
                clip.start();
                // 等待音频播放完毕
                ThreadSleepUtils.sleep(clip.getMicrosecondLength() / 1000);
            });
            // 关闭音频剪辑和音频流
//                clip.close();
//                audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    public static void main(String[] args) {

        try {

            URL url = ImageUtils.class.getClassLoader().getResource("MP3/3550.wav"  );
            // 从文件中加载音频流
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(URLDecoder.decode(url.getPath())));

            while (true){
                // 获取音频剪辑对象
                Clip clip = AudioSystem.getClip();

                // 打开音频剪辑并将音频流分配给剪辑
                clip.open(audioInputStream);

                // 开始播放音频
                clip.start();

                // 等待音频播放完毕
                Thread.sleep(clip.getMicrosecondLength() / 1000);
            }


            // 关闭音频剪辑和音频流
//            clip.close();
//            audioInputStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
