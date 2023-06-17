package com.pc.client;

import com.alibaba.fastjson.JSON;
import com.pc.client.gui.GamePanel;
import com.pc.client.gui.RoomPanel;
import com.pc.common.PropertiesUtils;
import com.pc.common.RpcDecoder;
import com.pc.common.RpcEncoder;
import com.pc.common.RpcProtocol;
import com.pc.common.msg.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @description:
 * @author: pangcheng
 * @time: 2023/6/2 14:48
 */
public class Client {

    private Logger log = LogManager.getLogger(Client.class);

    /**
     * 游戏通信连接管道
     */
    private  Channel gameChannel;

    /**
     * 房间连接管道
     */
    private  Channel roomChannel;

    private  Channel chatChannel;
    private  Channel taskChannel;

    private EventLoopGroup clientGroup = new NioEventLoopGroup();

    public Client() {

        try {
            roomChannel = createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void  initGameChannel(){
        try {
            gameChannel =  createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
            chatChannel =  createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
            taskChannel =  createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷新游戏连接
     */
    public  void refreshGameChannel(){
        System.out.println("重置连接");
        if(gameChannel!=null){
            gameChannel.close();
            chatChannel.close();
            taskChannel.close();
          }
        try {
            gameChannel =  createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
            chatChannel =  createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
            taskChannel =  createChannel(PropertiesUtils.get("serverUrl"),PropertiesUtils.getInteger("serverPort"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public  void sendMsg(Msg msg){
        RpcProtocol rpcProtocol = new RpcProtocol();
        String json = JSON.toJSONString(msg);
        rpcProtocol.setLen(json.getBytes().length);
        rpcProtocol.setContent(json.getBytes());
        gameChannel.writeAndFlush(rpcProtocol);
        log.info("cmd = sendMsg | msg ={}",msg );
    }

    /**
     * 发送聊天消息
     * @param msg
     */
    public  void sendChatMsg(Msg msg){
        RpcProtocol rpcProtocol = new RpcProtocol();
        String json = JSON.toJSONString(msg);
        rpcProtocol.setLen(json.getBytes().length);
        rpcProtocol.setContent(json.getBytes());
        chatChannel.writeAndFlush(rpcProtocol);
        log.info("cmd = sendChatMsg | msg ={}",msg );
    }

    /**
     * 注册 服务器定时任务刷新 消息
     * @param msg
     */
    public  void registerTask(Msg msg){
        RpcProtocol rpcProtocol = new RpcProtocol();
        String json = JSON.toJSONString(msg);
        rpcProtocol.setLen(json.getBytes().length);
        rpcProtocol.setContent(json.getBytes());
        chatChannel.writeAndFlush(rpcProtocol);
        log.info("cmd = sendMsg | msg ={}",msg );
    }

    /**
     * 发送房间
     * @param msg
     */
    public  void sendRoomMsg(Msg msg){
        RpcProtocol rpcProtocol = new RpcProtocol();
        String json = JSON.toJSONString(msg);
        rpcProtocol.setLen(json.getBytes().length);
        rpcProtocol.setContent(json.getBytes());
        roomChannel.writeAndFlush(rpcProtocol);
        log.info("cmd = sendRoomMsg | msg ={}",msg );
    }


    /**
     * 返回新的Channel
     * @param address ip地址
     * @param port 端口
     * @return channel
     * @throws InterruptedException exception
     */
    private Channel createChannel(String address, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .option(ChannelOption.SO_SNDBUF,512)//设置发送缓冲区
                .option(ChannelOption.SO_RCVBUF,1024*9)//设置发送缓冲区
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, false)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)

                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        /*
                            用于处理或截获通道的接收和发送数据。它提供了一种高级的截取过滤模式（类似serverlet中的filter功能）
                            通过 addLast 方法将一个一个的 ChannelHandler 添加到责任链上并给它们取个名称（不取也可以，Netty 会给它个默认名称），
                            这样就形成了链式结构。在请求进来或者响应出去时都会经过链上这些 ChannelHandler 的处理。
                            1）readerIdleTime：为读超时时间（即测试端一定时间内未接受到被测试端消息）
                            2）writerIdleTime：为写超时时间（即测试端一定时间内向被测试端发送消息）
                            3）allIdleTime：所有类型的超时时间    改选项用户 心跳。即每过多少秒发送一次包

                         */
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("Encoder", new RpcEncoder());
                        pipeline.addLast("Decoder", new RpcDecoder());
                        pipeline.addLast("clientHandler", new RpcClientSyncHandler());

                    }
                });
        return bootstrap.connect(address, port).sync().channel();
    }


}
