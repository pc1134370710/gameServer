package com.pc.server;

import com.alibaba.fastjson.JSON;
import com.pc.common.Constant;
import com.pc.common.RpcProtocol;
import com.pc.common.ServerCmd;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;
import com.pc.common.msg.UserRoleMsgData;
import com.pc.common.utils.ThreadSleepUtils;
import lombok.Data;

import java.util.*;
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
     * 电脑玩家
     */
    private Map<String, UserRoleMsgData> npcUser = new ConcurrentHashMap<>();


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
     * 是否添加机器人
     */
    private boolean addNpc ;
    /**
     * 房间名称
     */
    private String roomName;

    private AtomicBoolean isInit = new AtomicBoolean(false);
    private AtomicBoolean isOK= new AtomicBoolean(false);

    public RoomServer(String id ,int maxUserSize,boolean addNpc,String roomName){
        this.id = id;
        this.roomName = roomName;
        this.addNpc = addNpc;
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

        if(addNpc){
            // 初始化电脑玩家
            initNpcUser();
        }
        // 检测房间可用
        checkRoom();

    }

    /**
     * 追踪玩家,并攻击
     */
    private void traceUser(UserRoleMsgData user,UserRoleMsgData pcUser) {

        if(pcUser.getChasingUserId() !=null && !user.getUserId().equals(pcUser.getChasingUserId())){
            return;
        }

        // 如果上一次的攻击时间 + 冷却期 > 小于当前时间， 证明还不能攻击跟移动
        if(pcUser.getPcAttackTime()!=null &&
                (pcUser.getPcAttackTime()+Constant.pcAttackTimeGap) >System.currentTimeMillis()){
            return;
        }

        // 计算玩家与小怪的距离
        float distance = pcUser.distanceCalculator(user.getUserX(),user.getUserY());
        // 如果在感知范围内,开始追踪玩家
        if (distance <= Constant.senseRange ) {
            pcUser.setChasingUserId(user.getUserId());


            // 如果在攻击范围内,开始攻击玩家
            if (distance <= Constant.attackRange && user.getUserY().intValue() == pcUser.getUserY().intValue() ) {

                pcUser.setPcAttackTime(System.currentTimeMillis());
                // todo 这一步应该异步化w
                // 攻击玩家
                pcUser.setAttack(true);
                Msg msg = Msg.getMsg(ServerCmd.USER_ATTACK.getValue(), pcUser.getUserId(), pcUser);
                putMsg(msg);
                // 投入攻击队列中进行检测
                UserRoleMsgData temp = new UserRoleMsgData();
                temp.setUserX(pcUser.getUserX());
                temp.setUserY(pcUser.getUserY());
                temp.setAttack(pcUser.getAttack());
                temp.setUserId(pcUser.getUserId());
                temp.setDirection(pcUser.getDirection());
                temp.setHp(pcUser.getHp());
                temp.setMp(pcUser.getMp());
                // 重新new 一个对象， 防止引用传递，将该对象修改掉
                putAttackMsg(temp);

                ThreadSleepUtils.sleep(200);
                // 取消攻击
                pcUser.setAttack(false);
                msg = Msg.getMsg(ServerCmd.USER_ATTACK.getValue(), pcUser.getUserId(), pcUser);
                putMsg(msg);
            }else{
                // 开始追踪玩家,移动到攻击范围内
                // 判断玩家方向
                // 坐标轴， 左上是 0,0
                int monsterX=pcUser.getUserX(),monsterY=pcUser.getUserY();
                if(user.getUserX()-Constant.attackRange>pcUser.getUserX()){
                    // 在右边
                    monsterX+=Constant.NPCSpeed;
                    pcUser.setDirection(1);
                }else if(user.getUserX()-Constant.attackRange < pcUser.getUserX()){
                    monsterX-=Constant.NPCSpeed;
                    pcUser.setDirection(-1);
                }
                // todo   Constant.NPCSpeed  大于一的话， 会跟 用户坐标对不上，导致一直上下移动bug
                //  所以这个绝对值比较为了解决这一个问题，
                if(Math.abs(user.getUserY() - pcUser.getUserY())<=2){
                    monsterY = user.getUserY();
                }
                else if(user.getUserY()>pcUser.getUserY()){
                    // 在下边
                    monsterY+=Constant.NPCSpeed;
                }else if(user.getUserY()< pcUser.getUserY()){
                    monsterY-=Constant.NPCSpeed;
                }


                pcUser.setUserX(monsterX);
                pcUser.setUserY(monsterY);
                Msg msg = new Msg();
                msg.setCmd(ServerCmd.USER_MOVE.getValue());
                msg.setUserId(pcUser.getUserId());
                msg.setData(JSON.toJSONString(pcUser));
                this.putMsg(msg);
            }

        }
    }
    /**
     * 初始化 电脑玩家
     */
    private void initNpcUser() {

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(()->{
            if(!checkUserIsOk()){
                return;
            }
            // 场上电脑 少于5个
            if(npcUser.size() <2){
                // 添加
                for(int i = 0; i<2-npcUser.size();i++){
                    UserRoleMsgData userRoleMsgData  = new UserRoleMsgData().initNpc();
                    userRoleMsgData.setUserId("电脑玩家"+System.currentTimeMillis());
                    npcUser.put(userRoleMsgData.getUserId(),userRoleMsgData);
                    Msg msg = Msg.getMsg(ServerCmd.INIT_NPC.value, userRoleMsgData.getUserId(), userRoleMsgData);
                    putMsg(msg);
                }
            }


            Iterator<Map.Entry<String, UserRoleMsgData>> iterator = npcUser.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, UserRoleMsgData> next = iterator.next();
                if(next.getValue().getIsOver()){
                    // 已经死亡了, 移除该npc
                    iterator.remove();
                    ThreadSleepUtils.sleep(200);
                    //  发送 电脑玩家对出游戏命令
                    Msg msg = Msg.getMsg(ServerCmd.EXIT_GAME.getValue(), next.getValue().getUserId(), next.getValue());
                    putMsg(msg);
                    return;
                }
                try {
                    // 如果该电脑玩家已经在 追踪 玩家了
                    if(next.getValue().getChasingUserId() !=null){
                        traceUser(user.get(next.getValue().getChasingUserId()).getUserRoleMsgData(),next.getValue());
                    }
                    else{
                        // 获取所有符合追踪的玩家
                        List<UserModel> list = new ArrayList<>();
                        user.forEach((ku,vu)->{
                            int distance = next.getValue().distanceCalculator(vu.getUserRoleMsgData().getUserX(), vu.getUserRoleMsgData().getUserY());
                            if (distance <= Constant.senseRange ) {
                                list.add(vu);
                            }
                        });
                        if(list.size() !=0){
                            // 随机选择一个玩家 进行跟踪打击
                            int i = ThreadLocalRandom.current().nextInt(list.size());
                            traceUser(list.get(i).getUserRoleMsgData(),next.getValue());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }


        },3,70,TimeUnit.MILLISECONDS);
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

        // 检测电脑玩家
        Set<Map.Entry<String, UserRoleMsgData>> npc = npcUser.entrySet();
        for (Map.Entry<String, UserRoleMsgData> e : npc){
            String k = e.getKey();
            UserRoleMsgData userRoleMsgData =e.getValue();
            if(!k.equals(skillMsgData.getUserId())){
                try {
                    // 不是自己释放的技能 , 技能跟玩家站在一条线上, 并且玩家还没有死
                    if( skillMsgData.getY() == userRoleMsgData.getUserY()
                            && skillMsgData.rectangle().intersects(userRoleMsgData.rectangle())
                            && !userRoleMsgData.getIsOver()
                    ){
                        // 血量减少
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
                        Msg msg = Msg.getMsg(ServerCmd.START_GAME.getValue(), userRoleMoveMsgData.getUserId(), userRoleMoveMsgData);
                        // 发送消息
                        putMsg(msg);

                    });
                    isInit.set(true);
                    return;
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
                    List<Msg> list = new ArrayList<>();

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
//                                putMsg(msg);
                                list.add(msg);
                            }
                        }
                    }
                    //todo  检测电脑玩家
                    Set<Map.Entry<String, UserRoleMsgData>> npc = npcUser.entrySet();
                    for(Map.Entry<String, UserRoleMsgData> entry : npc){
                        if(!entry.getKey().equals(take.getUserId())){
                            if( take.getUserY().equals(entry.getValue().getUserY())
                                    &&  take.rectangle().intersects(entry.getValue().rectangle())){
                                // 血量减少
                                UserRoleMsgData userRoleMsgData = entry.getValue();
                                int hp = userRoleMsgData.getHp()  -   Constant.normalAttackHarm;
                                userRoleMsgData.setHp(hp<0?0:hp);
                                userRoleMsgData.setIsOver(hp<=0);
                                Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), entry.getKey(), userRoleMsgData);
//                                putMsg(msg);
                                list.add(msg);
                            }
                        }
                    }
                    // 统一投递
                    list.forEach(a->putMsg(a));
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
        if(isOK.get()) {
            return true;
        }
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
                    queue.clear();
                    attackQueue.clear();
                    npcUser.clear();
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

    /**
     * 投递 攻击检测队列
     * @param userRoleMsgData
     */
    public void putAttackMsg(UserRoleMsgData userRoleMsgData){
        try {
            attackQueue.put(userRoleMsgData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Msg getMsg(){
        try {
//            System.out.println("队列大小："+queue.size());
            Msg take = queue.take();
            return take;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("消息为空");
    }

}
