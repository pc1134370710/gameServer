package com.pc.server;

import com.pc.common.Constant;
import com.pc.common.RpcProtocol;
import com.pc.common.ServerCmd;
import com.pc.common.msg.MonsterMsgData;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;
import com.pc.common.msg.UserRoleMsgData;
import lombok.Data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:  服务器房间
 * @author: pangcheng
 * @create: 2023-06-02 22:23
 **/
@Data
public class RoomServer {

    /**
     * 房间内的用户， key : userId
     */
    private  Map<String,UserModel> user = new ConcurrentHashMap<>();

    /**
     * 房间内技能情况
     */
    private Map<String, SkillMsgData> skillMsgDataMap = new ConcurrentHashMap<>();


    /**
     * 房间id
     */
    private String id;
    private BlockingQueue<Msg> queue = new LinkedBlockingQueue<>();
    /**
     * 普通攻击检测队列
     */
    private BlockingQueue<UserRoleMsgData> attackQueue = new LinkedBlockingQueue<>();

    private ThreadPoolExecutor threadPoolExecutors = new ThreadPoolExecutor(10,20,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1024));
    /**
     * 定时
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);

    // 最大游戏人数量
    private int maxUserSize = 2;

    /**
     * 房间内的小怪
     */
    private List<MonsterMsgData> monsterMsgData = new CopyOnWriteArrayList<>();
    private AtomicBoolean isInit = new AtomicBoolean(false);
    private AtomicBoolean isOK= new AtomicBoolean(false);

    public RoomServer(String id ,int maxUserSize){
        this.id = id;
        this.maxUserSize = maxUserSize;
        // 获取消息发送
        sendMsg();
        // 初始化用户角色
        initUserRole();

        // todo 可以优化成， 有用户点击加入房间后在跑这些任务
        // 检测技能释放
        checkMoveSkill();
        // 检测普攻
        checkUserAttack();
        // 玩家状态检测， 以及恢复MP 蓝条
        recoverMpAndCheckUserStatus();

        // 随机boss


        // 服务器集群通信

        // 检测房间可用
        checkRoom();

    }

    /**
     * 玩家状态检测， 以及恢复MP 蓝条
     */
    private void recoverMpAndCheckUserStatus() {
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(()->{
            // 全部就绪， 并且 初始化房间
            if(checkUserIsOk() && isInit.get()){
                Map<String,UserModel> temp=new ConcurrentHashMap<>();
                user.forEach((k,v)->{
                    UserRoleMsgData userRoleMsgData = v.getUserRoleMsgData();
                    if(userRoleMsgData.getMp()<100){
                        userRoleMsgData.setMp(userRoleMsgData.getMp()+10);
                        temp.put(k,v);
                    }
                });
                // 恢复蓝条
                temp.forEach((k,v)->{
                    UserRoleMsgData userRoleMsgData = v.getUserRoleMsgData();
                    Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), k, userRoleMsgData);
                    putMsg(msg);
                });

            }
        },1,3,TimeUnit.SECONDS);

    }

    /**
     * 检测技能
     */
    private void checkMoveSkill() {
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(()->{
            Iterator<Map.Entry<String, SkillMsgData>> iterator = skillMsgDataMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SkillMsgData> entry = iterator.next();
                // 技能移动
                entry.getValue().move();
                // 检测技能是否有效
                if(entry.getValue().checkSkill()){
                    // 检测是否跟用户发生碰撞， 并扣血
                    if(detectSkillHit(entry.getValue())){
                        // 移除该技能
                        iterator.remove();
                    }
                }else{
                    // 技能失效
                    Msg msg = Msg.getMsg(ServerCmd.SKILL_DELETE.getValue(), entry.getValue().getUserId(), entry.getValue());
                    putMsg(msg);
                    // 移除该技能
                    iterator.remove();
                }
            }
            skillMsgDataMap.forEach((k,v)->{
                Msg msg = Msg.getMsg(ServerCmd.SKILL_MOVE.getValue(), v.getUserId(), v);
                putMsg(msg);
            });

        },1,10,TimeUnit.MILLISECONDS);
    }

    /**
     * 检测 技能是否命中了 某个玩家
     * @param skillMsgData
     */
    private boolean detectSkillHit(SkillMsgData skillMsgData) {
        Set<Map.Entry<String, UserModel>> entries = user.entrySet();
        for (Map.Entry<String, UserModel> e : entries){
            String k = e.getKey();
            UserModel userModel =e.getValue();
            if(!k.equals(skillMsgData.getUserId())){
                try {
                    // 不是自己释放的技能 , 技能跟玩家站在一条线上, 并且玩家还没有死
                    if( skillMsgData.getY() == userModel.getUserRoleMsgData().getUserY()
                            && skillMsgData.rectangle().intersects(userModel.getUserRoleMsgData().rectangle())
                        && !userModel.getUserRoleMsgData().getIsOver()
                    ){
                        // 血量减少
                        UserRoleMsgData userRoleMsgData = userModel.getUserRoleMsgData();
                        int hp = userRoleMsgData.getHp()  -   Constant.skillHarm;
                        userRoleMsgData.setHp(hp<0?0:hp);
                        userRoleMsgData.setIsOver(hp<=0);
                        Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), k, userRoleMsgData);
                        putMsg(msg);

                        // 发送技能失效
                        Msg msg2 = Msg.getMsg(ServerCmd.SKILL_DELETE.getValue(), k, skillMsgData);
                        putMsg(msg2);
                        return true;
                    }
                }catch (Exception re){
                    re.printStackTrace();
                }

            }
        }

        return false;
    }

    /**
     * 初始化用户角色
     */
    private void initUserRole() {
        threadPoolExecutors.execute(()->{
            while (true){
                // 全部就绪， 并且未初始化房间
                if(checkUserIsOk() && !isInit.get()){
                    // 如果已经初始化号房间了，那么就可以开始 初始化用户角色
                    user.entrySet().forEach(u->{
                        UserRoleMsgData userRoleMoveMsgData = u.getValue().getUserRoleMsgData();
                        userRoleMoveMsgData.setUserId(u.getKey());
                        Msg msg = Msg.getMsg(ServerCmd.INIT_USER_ROLE.getValue(), userRoleMoveMsgData.getUserId(), userRoleMoveMsgData);
                        // 发送消息
                        putMsg(msg);

                    });
                    isInit.set(true);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

            }
        });

    }


    /**
     * 检测所有玩家的普通攻击 是否命中
     */
    private void checkUserAttack(){
        threadPoolExecutors.execute(()->{
            while (true){
                try {
                    // take 发动攻击的人
                    UserRoleMsgData take = attackQueue.take();
                    Set<Map.Entry<String, UserModel>> entries = user.entrySet();
                    for(Map.Entry<String, UserModel> entry : entries){
                        if(!entry.getKey().equals(take.getUserId())){
                            // 不是自己的普通攻击,在同一条直线上
                            if( take.getUserY().equals(entry.getValue().getUserRoleMsgData().getUserY())
                                    &&  take.rectangle().intersects(entry.getValue().getUserRoleMsgData().rectangle())){
                                // 血量减少
                                UserRoleMsgData userRoleMsgData = entry.getValue().getUserRoleMsgData();
                                int hp = userRoleMsgData.getHp()  -   Constant.normalAttackHarm;
                                userRoleMsgData.setHp(hp<0?0:hp);
                                userRoleMsgData.setIsOver(hp<=0);
                                Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), entry.getKey(), userRoleMsgData);
                                putMsg(msg);
                            }
                        }
                    }


                } catch (InterruptedException e) {
                    System.out.println("检测普通失败");
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 检测用户是否都已准备就绪
     * @return
     */
    private boolean checkUserIsOk(){
        // 如果已经准备就绪了就不用再往下判断了
        if(isOK.get()) return true;
        if(user.size() == maxUserSize){
            Set<Map.Entry<String, UserModel>> entries = user.entrySet();
            int cnt =0;
            for(Map.Entry<String, UserModel> entry : entries){
                cnt +=entry.getValue().getStart();
            }
            if(cnt == maxUserSize){
                isOK.set(true);
                return true;
            }
        }
        return false;
    }


    /**
     * 检测房间可用
     */
    private void checkRoom(){
        threadPoolExecutors.execute(()->{
            while (true){
                // 房间可用
                if(user.size() == 0 && isInit.get()){
                    System.out.println("重置房间：" + id);
                    isInit.set(false);
                    isOK.set(false);
                    user.clear();
                    monsterMsgData.clear();
                    queue.clear();
                    attackQueue.clear();
                    // 给所有连接用户发送 刷新房间消息


                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }

        });
    }


    /**
     * 队列 给房间内所以用户发送消息发送消息
     */
    private void sendMsg(){
        threadPoolExecutors.execute(()->{
            while (true){
                Msg msg = getMsg();
//                System.out.println("房间"+id+"发送消息"+msg);
                Set<Map.Entry<String, UserModel>> entries = user.entrySet();
                for(Map.Entry<String, UserModel> entry : entries){
                    entry.getValue().getChannel().writeAndFlush(RpcProtocol.getRpcProtocol(msg));
                }
            }
        });
    }
    /**
     * 将消息加入房间队列中
     * @param msg
     */
    public void putMsg(Msg msg){
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            System.out.println("发送消息失败");
            e.printStackTrace();
        }
    }
    private Msg getMsg(){
        try {
            Msg take = queue.take();
            return take;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("消息为空");
    }

}
