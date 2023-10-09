package com.pc.server.model;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pc.common.constant.Constant;
import com.pc.common.prtotcol.RpcProtocol;
import com.pc.common.prtotcol.ServerCmd;
import com.pc.common.msg.Msg;
import com.pc.common.msg.SkillMsgData;
import com.pc.common.msg.UserRoleMsgData;
import io.netty.channel.Channel;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 服务器房间
 * @author: pangcheng
 * @create: 2023-06-02 22:23
 **/
@Data
public class RoomServer {

    private static final Logger log = LogManager.getLogger(RoomServer.class);

    /**
     * 房间id
     */
    private String id;
    /**
     * 最大游戏人数量
     */
    private int maxUserSize = 2;
    /**
     * 是否添加机器人
     */
    private boolean addNpc;
    /**
     * 房间名称
     */
    private String roomName;
    /**
     * 房间是否已经 开始游戏
     */
    private AtomicBoolean isInit = new AtomicBoolean(false);
    /**
     * 用户准备就绪
     */
    private AtomicBoolean isOK = new AtomicBoolean(false);

    /**
     * 房间内的用户， key : userId
     */
//    private Cache<String, UserModel> user = Caffeine.newBuilder().build();
    private Map<String, UserModel> user = new ConcurrentHashMap<>();

    /**
     * 房间内技能情况
     */
    private Map<String, SkillMsgData> skillMsgDataMap = new ConcurrentHashMap<>();
    /**
     * 电脑玩家
     */
    private Map<String, UserRoleMsgData> npcUser = new ConcurrentHashMap<>();


    private BlockingQueue<Msg> queue = new LinkedBlockingQueue<>();
    /**
     * 定时任务消息队列
     */
    private BlockingQueue<Msg> taskMsgQueue = new LinkedBlockingQueue<>();
    /**
     * 聊天消息
     */
    private BlockingQueue<Msg> chatMsgQueue = new LinkedBlockingQueue<>();
    /**
     * 普通攻击检测队列
     */
    private BlockingQueue<UserRoleMsgData> attackQueue = new LinkedBlockingQueue<>();

    private ThreadPoolExecutor threadPoolExecutors = new ThreadPoolExecutor(10, 20
            , 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024));
    /**
     * 定时
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);

    public RoomServer(String id, int maxUserSize, boolean addNpc, String roomName) {
        this.id = id;
        this.roomName = roomName;
        this.addNpc = addNpc;
        this.maxUserSize = maxUserSize;
        // 获取消息发送
        sendMsg();
        taskSendMsgQueue();
        chatSendMsgQueue();
        // 初始化用户角色
        initUserRole();

        // 检测房间可用
        checkRoom();
    }

    /**
     * 追踪玩家,并攻击
     * 如果存在障碍物， 可以使用 bfs 寻路算法， 将路径存放起来一条一条发送数据
     *
     * @param user   玩家
     * @param pcUser 电脑
     */
    private void traceUser(UserRoleMsgData user, UserRoleMsgData pcUser) {
        if (pcUser.getChasingUserId() != null && !user.getUserId().equals(pcUser.getChasingUserId())) {
            return;
        }
        // 如果上一次的攻击时间 + 冷却期 > 小于当前时间， 证明还不能攻击跟移动
        if (pcUser.getPcAttackTime() != null &&
                (pcUser.getPcAttackTime() + Constant.pcAttackTimeGap) > System.currentTimeMillis()) {
            return;
        }
        // 计算玩家与小怪的距离
        float distance = pcUser.distanceCalculator(user.getUserX(), user.getUserY());
        // 如果在感知范围内,开始追踪玩家
        if (distance <= Constant.senseRange) {
            pcUser.setChasingUserId(user.getUserId());
            // 如果在攻击范围内,开始攻击玩家
            if (distance <= Constant.attackRange && user.getUserY().intValue() == pcUser.getUserY().intValue()) {
                pcUser.setPcAttackTime(System.currentTimeMillis());
                // TODO 这一步应该异步化
                threadPoolExecutors.execute(() -> {
                    // 攻击玩家
                    pcUser.setAttack(true);
                    Msg msg = Msg.getMsg(ServerCmd.USER_ATTACK.getValue(), pcUser.getUserId(), pcUser);
                    putTaskMsg(msg);
                    // 投入攻击队列中进行检测
                    UserRoleMsgData temp = new UserRoleMsgData();
                    temp.setUserX(pcUser.getUserX());
                    temp.setUserY(pcUser.getUserY());
                    temp.setAttack(pcUser.getAttack());
                    temp.setUserId(pcUser.getUserId());
                    temp.setDirection(pcUser.getDirection());
                    temp.setUserId(pcUser.getUserId());
                    temp.setIsNpc(pcUser.getIsNpc());
                    temp.setHp(pcUser.getHp());
                    temp.setMp(pcUser.getMp());
                    // 重新new 一个对象， 防止引用传递，将该对象修改掉
                    putAttackMsg(temp);

                    ThreadUtil.sleep(200);
                    // 取消攻击
                    pcUser.setAttack(false);
                    msg = Msg.getMsg(ServerCmd.USER_ATTACK.getValue(), pcUser.getUserId(), pcUser);
                    putTaskMsg(msg);
                });
                return;
            }

            // 开始追踪玩家,移动到攻击范围内
            // 判断玩家方向
            // 坐标轴， 左上是 0,0
            int monsterX = pcUser.getUserX();
            int monsterY = pcUser.getUserY();
            if (user.getUserX() - Constant.attackRange > pcUser.getUserX()) {
                // 在右边
                monsterX += Constant.NPCSpeed;
                pcUser.setDirection(1);
            } else if (user.getUserX() - Constant.attackRange < pcUser.getUserX()) {
                monsterX -= Constant.NPCSpeed;
                pcUser.setDirection(-1);
            }
            //   Constant.NPC Speed  大于一的话， 会跟 用户坐标对不上，导致一直上下移动 BUG，所以这个绝对值比较为了解决这一个问题
            if (Math.abs(user.getUserY() - pcUser.getUserY()) <= 2) {
                monsterY = user.getUserY();
            } else if (user.getUserY() > pcUser.getUserY()) {
                // 在下边
                monsterY += Constant.NPCSpeed;
            } else if (user.getUserY() < pcUser.getUserY()) {
                monsterY -= Constant.NPCSpeed;
            }

            pcUser.setUserX(monsterX);
            pcUser.setUserY(monsterY);
            Msg msg = new Msg();
            msg.setCmd(ServerCmd.USER_MOVE.getValue());
            msg.setUserId(pcUser.getUserId());
            msg.setData(JSON.toJSONString(pcUser));
            this.putTaskMsg(msg);

        }
    }

    /**
     * 初始化 电脑玩家
     */
    private void initNpcUser() {
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            try {
                // 玩家准备就绪， 并且玩家以及初始化后才初始化 npc
                if (!checkUserIsOk() && !isInit.get()) {
                    return;
                }
                // 场上电脑 少于5个
                if (npcUser.size() < 2) {
                    // 添加
                    for (int i = 0; i < 2 - npcUser.size(); i++) {
                        UserRoleMsgData userRoleMsgData = new UserRoleMsgData().initNpc();
                        userRoleMsgData.setUserId("电脑" + System.currentTimeMillis());
                        npcUser.put(userRoleMsgData.getUserId(), userRoleMsgData);
                        Msg msg = Msg.getMsg(ServerCmd.INIT_NPC.value, userRoleMsgData.getUserId(), userRoleMsgData);
                        putTaskMsg(msg);
                    }
                }

                Iterator<Map.Entry<String, UserRoleMsgData>> iterator = npcUser.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, UserRoleMsgData> next = iterator.next();
                    if (next.getValue().getIsOver().equals(Boolean.TRUE)) {
                        // 已经死亡了, 移除该npc
                        iterator.remove();
                        // 短暂保留 死亡画面
                        ThreadUtil.sleep(400);
                        //  发送 电脑玩家对出游戏命令
                        Msg msg = Msg.getMsg(ServerCmd.EXIT_GAME.getValue(), next.getValue().getUserId(), next.getValue());
                        putTaskMsg(msg);
                        return;
                    }
                    // 如果该电脑玩家已经在 追踪 玩家了
                    if (next.getValue().getChasingUserId() != null) {
                        UserModel userModel = user.get(next.getValue().getChasingUserId());
                        if (userModel == null) {
                            next.getValue().setChasingUserId(null);
                            // 为空的情况， 可能是玩家 中途退出了游戏
                            return;
                        }
                        if (userModel.getUserRoleMsgData().getIsOver()) {
                            // 玩家死亡， npc 继续最终其他玩家
                            next.getValue().setChasingUserId(null);
                            return;
                        }
                        traceUser(userModel.getUserRoleMsgData(), next.getValue());
                    } else {
                        // 获取所有符合追踪的玩家
                        List<UserModel> list = new ArrayList<>();
                        user.forEach((ku, vu) -> {
                            int distance = next.getValue().distanceCalculator(vu.getUserRoleMsgData().getUserX(), vu.getUserRoleMsgData().getUserY());
                            if (distance <= Constant.senseRange) {
                                list.add(vu);
                            }
                        });
                        if (list.size() != 0) {
                            // 随机选择一个玩家 进行跟踪打击
                            int i = ThreadLocalRandom.current().nextInt(list.size());
                            traceUser(list.get(i).getUserRoleMsgData(), next.getValue());
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * 玩家状态检测， 以及恢复MP 蓝条
     */
    private void recoverMpAndCheckUserStatus() {
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            // 全部就绪， 并且 初始化房间
            if (checkUserIsOk() && isInit.get()) {
                Map<String, UserModel> temp = new ConcurrentHashMap<>();
                user.forEach((k, v) -> {
                    UserRoleMsgData userRoleMsgData = v.getUserRoleMsgData();
                    if (userRoleMsgData.getMp() < 100) {
                        userRoleMsgData.setMp(userRoleMsgData.getMp() + 10);
                        temp.put(k, v);
                    }
                });
                // 恢复蓝条
                temp.forEach((k, v) -> {
                    UserRoleMsgData userRoleMsgData = v.getUserRoleMsgData();
                    Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), k, userRoleMsgData);
                    putTaskMsg(msg);
                });
            }
        }, 1, 3, TimeUnit.SECONDS);

    }

    /**
     * 检测技能
     */
    private void checkMoveSkill() {
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            Iterator<Map.Entry<String, SkillMsgData>> iterator = skillMsgDataMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SkillMsgData> entry = iterator.next();
                SkillMsgData skillMsgData = entry.getValue();
                // 技能移动
                skillMsgData.move();
                // 检测技能是否有效 && 检测是否跟用户发生碰撞， 并扣血
                if (skillMsgData.checkSkill()) {
                    if (detectSkillHit(skillMsgData)) {
                        // 移除该技能
                        iterator.remove();
                    }
                } else {
                    // 技能失效
                    Msg msg = Msg.getMsg(ServerCmd.SKILL_DELETE.getValue(), skillMsgData.getUserId(), skillMsgData);
                    putTaskMsg(msg);
                    // 移除该技能
                    iterator.remove();
                }
            }
            // 发送移动技能消息给用户
            skillMsgDataMap.forEach((k, v) -> {
                Msg msg = Msg.getMsg(ServerCmd.SKILL_MOVE.getValue(), v.getUserId(), v);
                putTaskMsg(msg);
            });
        }, 1, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * 检测 技能是否命中了 某个玩家
     * 遍历所有玩家，
     * 排除自己， 判断技能是否跟玩家在同一条线上， 玩家释放已经死亡， 并且是否 发生矩形碰撞
     *
     * @return true : 技能已经失效， false :  技能未被失效
     */
    private boolean detectSkillHit(SkillMsgData skillMsgData) {
        Set<Map.Entry<String, UserModel>> entries = user.entrySet();
        for (Map.Entry<String, UserModel> e : entries) {
            UserModel userModel = e.getValue();
            UserRoleMsgData userRoleMsgData = userModel.getUserRoleMsgData();
            // 不是自己释放的技能
            if (!e.getKey().equals(skillMsgData.getUserId())) {
                //碰撞检测， 并且发送碰撞结果 消息
                if (collisionDetectionAndSendMsg(skillMsgData, userRoleMsgData)) {
                    // 技能失效
                    return true;
                }
            }
        }
        UserRoleMsgData npcUserMsaData = npcUser.get(skillMsgData.getUserId());
        if (npcUserMsaData != null) {
            // 电脑玩家 对电脑玩家不生效
            // 返回 false 代表技能没有失效
            return false;
        }
        // 检测电脑玩家
        Set<Map.Entry<String, UserRoleMsgData>> npc = npcUser.entrySet();
        for (Map.Entry<String, UserRoleMsgData> e : npc) {
            UserRoleMsgData userRoleMsgData = e.getValue();
            //  技能跟玩家站在一条线上, 并且玩家还没有死， 发送碰撞
            if (collisionDetectionAndSendMsg(skillMsgData, userRoleMsgData)) {
                return true;
            }
        }
        // 返回 false 代表技能没有失效
        return false;
    }

    /**
     * 封装公共代码, 碰撞检测， 并且发送碰撞结果 消息
     *
     * @param skillMsgData    技能
     * @param userRoleMsgData 用户
     * @return true
     */
    private boolean collisionDetectionAndSendMsg(SkillMsgData skillMsgData, UserRoleMsgData userRoleMsgData) {
        // TODO  矩形碰撞，应该拿玩家的非攻击状态下的 矩形，不然一定概率性会出现，玩家在攻击时，被攻击的范围增大
        //  技能跟玩家站在一条线上，并且玩家还没有死，发送碰撞
        if (skillMsgData.getY() == userRoleMsgData.getUserY()
                && skillMsgData.rectangle().intersects(userRoleMsgData.commonRectangle())
                && !userRoleMsgData.getIsOver()) {
            // 血量减少
            int hp = userRoleMsgData.getHp() - Constant.skillHarm;
            userRoleMsgData.setHp(Math.max(hp, 0));
            userRoleMsgData.setIsOver(hp <= 0);
            Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), userRoleMsgData.getUserId(), userRoleMsgData);
            putTaskMsg(msg);
            // 发送技能失效
            Msg msg2 = Msg.getMsg(ServerCmd.SKILL_DELETE.getValue(), userRoleMsgData.getUserId(), skillMsgData);
            putTaskMsg(msg2);
            if (userRoleMsgData.getIsOver()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("(系统消息) ")
                        .append(skillMsgData.getUserId())
                        .append("将 玩家 ")
                        .append(userRoleMsgData.getUserId())
                        .append(" 击杀");
                Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), skillMsgData.getUserId(), id, stringBuilder.toString());
                Msg msg1 = Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), skillMsgData.getUserId(), id, stringBuilder.toString());
                putChatMsg(msg1);
                String msg2Data = "(系统消息) " + userRoleMsgData.getUserId() + " 真是菜爆了";
                Msg msg2da = Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), userRoleMsgData.getUserId(), id, msg2Data);
                putChatMsg(msg2da);

            }

            return true;
        }
        return false;
    }

    /**
     * 初始化用户角色
     */
    private void initUserRole() {
        threadPoolExecutors.execute(() -> {
            while (true) {
                // 全部就绪， 并且未初始化房间
                if (checkUserIsOk() && !isInit.get()) {
                    // 如果已经初始化号房间了，那么就可以开始 初始化用户角色
                    user.forEach((key, value) -> {
                        UserRoleMsgData userRoleMoveMsgData = value.getUserRoleMsgData();
                        userRoleMoveMsgData.setUserId(key);
                        Msg msg = Msg.getMsg(ServerCmd.START_GAME.getValue(), userRoleMoveMsgData.getUserId(), userRoleMoveMsgData);
                        // 发送消息
                        putMsg(msg);

                    });
                    // 将房间状态设置成 以初始化
                    isInit.set(true);

                    if (addNpc) {
                        // 初始化电脑玩家
                        initNpcUser();
                    }
                    // 检测普攻， 先转换成，收到消息时处理
                    checkUserAttack();
                    // 检测技能释放
                    checkMoveSkill();
                    // 玩家状态检测， 以及恢复MP 蓝条
                    recoverMpAndCheckUserStatus();
                }
                ThreadUtil.sleep(1000);

            }
        });

    }

    /**
     * 检测所有玩家的普通攻击 是否命中
     */
    private void checkUserAttack() {
        threadPoolExecutors.execute(() -> {
            while (true) {
                try {
                    // take 发动攻击的人
                    UserRoleMsgData take = attackQueue.take();
                    Set<Map.Entry<String, UserModel>> entries = user.entrySet();
                    for (Map.Entry<String, UserModel> entry : entries) {
                        if (!entry.getKey().equals(take.getUserId())) {
                            // TODO  矩形碰撞 应该 拿 玩家的非攻击状态下的 矩形， 不然一定概率性会出现， 玩家在攻击时， 被攻击的范围增大
                            // 不是自己的普通攻击,在同一条直线上， 玩家没有死亡
                            if (take.getUserY().equals(entry.getValue().getUserRoleMsgData().getUserY())
                                    && take.attackRectangle().intersects(entry.getValue().getUserRoleMsgData().commonRectangle())
                                    && !entry.getValue().getUserRoleMsgData().getIsOver()) {
                                // 血量减少
                                UserRoleMsgData userRoleMsgData = entry.getValue().getUserRoleMsgData();
                                int hp = userRoleMsgData.getHp() - Constant.normalAttackHarm;
                                userRoleMsgData.setHp(Math.max(hp, 0));
                                userRoleMsgData.setIsOver(hp <= 0);
                                Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), entry.getKey(), userRoleMsgData);
                                putMsg(msg);
                                if (userRoleMsgData.getIsOver()) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("(系统消息) ")
                                            .append(take.getUserId())
                                            .append("将 玩家 ")
                                            .append(entry.getKey())
                                            .append(" 击杀");
                                    Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), entry.getKey(), id, stringBuilder.toString());
                                    Msg msg1 = Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), entry.getKey(), id, stringBuilder.toString());

                                    String msg2Data = "(系统消息) " + entry.getKey() + " 真是菜爆了";
                                    putChatMsg(msg1);
                                    Msg msg2 = Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), entry.getKey(), id, msg2Data);
                                    putChatMsg(msg2);
                                }

                                // 只能打到一个人身上
                                // continue;
                            }
                        }
                    }
                    // 电脑玩家不可以 对电脑玩家造成伤害哦
                    if (!take.getIsNpc()) {
                        //  检测电脑玩家
                        Set<Map.Entry<String, UserRoleMsgData>> npc = npcUser.entrySet();
                        for (Map.Entry<String, UserRoleMsgData> entry : npc) {
                            if (take.getUserY().equals(entry.getValue().getUserY())
                                    && take.attackRectangle().intersects(entry.getValue().commonRectangle())
                                    && !entry.getValue().getIsOver()) {
                                // 血量减少
                                UserRoleMsgData userRoleMsgData = entry.getValue();
                                int hp = userRoleMsgData.getHp() - Constant.normalAttackHarm;
                                userRoleMsgData.setHp(Math.max(hp, 0));
                                userRoleMsgData.setIsOver(hp <= 0);
                                Msg msg = Msg.getMsg(ServerCmd.REFRESH_USER_INFO.getValue(), entry.getKey(), userRoleMsgData);
                                putMsg(msg);

                                if (userRoleMsgData.getIsOver()) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("(系统消息) ")
                                            .append(take.getUserId())
                                            .append("将 ")
                                            .append(entry.getKey())
                                            .append(" 击杀");
                                    Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), entry.getKey(), id, stringBuilder.toString());
                                    Msg msg1 = Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), entry.getKey(), id, stringBuilder.toString());
                                    putChatMsg(msg1);
//                                    String msg2Data ="(系统消息) "+ entry.getKey() +" 真是菜爆了";
//                                    Msg msg2 = Msg.getMsg(ServerCmd.CHAT_MSG.getValue(), entry.getKey(), id, msg2Data);
//                                    putChatMsg(msg2);
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    log.error("cmd = checkUserAttack | msg = 普通攻击检测失败 | roomId ={} ", e);
                }
            }
        });
    }


    /**
     * 检测用户是否都已准备就绪
     */
    private boolean checkUserIsOk() {
        // 如果已经准备就绪了就不用再往下判断了
        if (isOK.get()) {
            return true;
        }
        if (user.size() == maxUserSize) {
            Set<Map.Entry<String, UserModel>> entries = user.entrySet();
            int cnt = 0;
            for (Map.Entry<String, UserModel> entry : entries) {
                cnt += entry.getValue().getStart();
            }
            if (cnt == maxUserSize) {
                isOK.set(true);
                return true;
            }
        }
        return false;
    }


    /**
     * 检测房间可用
     */
    private void checkRoom() {
        threadPoolExecutors.execute(() -> {
            while (true) {
                // 房间可用
                if (user.size() == 0 && isInit.get()) {
                    log.info("cmd =checkRoom | msg = 重置房间 | roomId ={}", id);
                    isInit.set(false);
                    isOK.set(false);
                    user.clear();
                    queue.clear();
                    attackQueue.clear();
                    npcUser.clear();
                    // 给所有连接用户发送 刷新房间消息
                }
                ThreadUtil.sleep(5000);
            }

        });
    }


    /**
     * 队列 给房间内所以用户发送消息发送消息
     */
    private void sendMsg() {
        threadPoolExecutors.execute(() -> {
            while (true) {
                Msg msg = getMsg();
                Set<Map.Entry<String, UserModel>> entries = user.entrySet();
                for (Map.Entry<String, UserModel> entry : entries) {
                    entry.getValue().getChannel().writeAndFlush(RpcProtocol.getRpcProtocol(msg));
                }
            }
        });
    }

    /**
     * 定时任务消息发送
     */
    private void taskSendMsgQueue() {
        threadPoolExecutors.execute(() -> {
            while (true) {
                try {
                    Msg msg = taskMsgQueue.take();
                    Set<Map.Entry<String, UserModel>> entries = user.entrySet();
                    for (Map.Entry<String, UserModel> entry : entries) {
                        entry.getValue().getTaskChannel().writeAndFlush(RpcProtocol.getRpcProtocol(msg));
                    }
                } catch (InterruptedException e) {
                    log.error("cmd = taskSendMsgQueue | msg = 发送定时任务消息失败", e);
                }

            }
        });
    }

    private void chatSendMsgQueue() {
        threadPoolExecutors.execute(() -> {
            while (true) {
                try {
                    Msg msg = chatMsgQueue.take();
                    Set<Map.Entry<String, UserModel>> entries = user.entrySet();
                    for (Map.Entry<String, UserModel> entry : entries) {
                        entry.getValue().getChatChannel().writeAndFlush(RpcProtocol.getRpcProtocol(msg));
                    }
                } catch (InterruptedException e) {
                    log.error("cmd = chatSendMsgQueue | msg = 发送聊天消息失败", e);
                }
            }
        });
    }

    /**
     * 将消息加入房间队列中
     */
    public void putMsg(Msg msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            log.error("cmd = putMsg | roomId ={} | msg = 消息加入队列中失败 | msg = {} ", id, msg, e);
        }
    }

    public void putTaskMsg(Msg msg) {
        try {
            taskMsgQueue.put(msg);
        } catch (InterruptedException e) {
            log.error("cmd = putTaskMsg | roomId ={} | msg = 消息加入队列中失败 | msg = {} ", id, msg, e);
        }
    }

    public void putChatMsg(Msg msg) {
        try {
            chatMsgQueue.put(msg);
        } catch (InterruptedException e) {
            log.error("cmd = putChatMsg | roomId ={} | msg = 消息加入队列中失败 | msg = {} ", id, msg, e);
        }
    }

    /**
     * 投递 攻击检测队列
     */
    public void putAttackMsg(UserRoleMsgData userRoleMsgData) {
        try {
            attackQueue.put(userRoleMsgData);
        } catch (InterruptedException e) {
            log.error("cmd = putAttackMsg | roomId ={} | msg = 投递 攻击检测队列 | userRoleMsgData = {} ", id, userRoleMsgData, e);
        }
    }

    private Msg getMsg() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            log.error("cmd = getMsg | roomId ={} | msg = 获取消息失败  ", id, e);
        }
        throw new RuntimeException("消息为空");
    }

}
