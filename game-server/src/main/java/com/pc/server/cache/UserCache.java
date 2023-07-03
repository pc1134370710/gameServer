package com.pc.server.cache;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.pc.server.model.UserModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author Kevin
 * @className: UserCache
 * @description: 用户通道缓存
 * @date 2023/6/20 10:38
 */
public class UserCache {

    private static final Logger log = LogManager.getLogger(UserCache.class);

    private static final Map<String/* userId */, String/* ChannelId */> channelRepository = Maps.newConcurrentMap();
    private static final LoadingCache<String, UserModel> repository = Caffeine.newBuilder().build(key -> {
        UserModel info = new UserModel();
        info.setChannelId(key);
        return info;
    });

    /**
     * 关闭一个通道
     */
    public static void invalidate(String channelId) {
        repository.invalidate(channelId);
    }

    /**
     * 创建一个通道
     */
    public static UserModel createChannel(UserModel channel) {
        repository.put(channel.getChannelId(), channel);
        return channel;
    }

    /**
     * 获取一个通道信息
     */
    public static UserModel getChannelInfo(String channelId) {
        if (channelId == null) return null;
        return repository.get(channelId);
    }

    /**
     * 获取缓存数量
     */
    public static Long countRepositorySize() {
        return repository.estimatedSize();
    }

    /***
     * 绑定通道Id与bindId
     */
    public static void bindChannel(String channelId, String userId) {
        if (channelRepository.containsKey(userId)) {
            UserModel old = getChannelInfo(channelRepository.get(userId));
            log.debug("客户端:{} 已经连接过服务端,原连接信息:{} ，当前连接信息:{}", userId, old, getChannelInfo(channelId));
            if (ObjectUtil.isNotEmpty(old) && !old.getChannelId().equalsIgnoreCase(channelId)) {
                if (old.getChannel() != null && old.getChannel().isActive()) {
                    log.info("客户端:{} 原连接信息为:{},连接活跃中,即将关闭原连接, 替换为当前连接:{}", userId, old, getChannelInfo(channelId));
                    old.close();
                    invalidate(old.getChannelId());
                }
            }
        }
        channelRepository.put(userId, channelId);
    }

    public static String getChannelByUserId(String bindId) {
        if (StrUtil.isNotBlank(bindId)) {
            return channelRepository.get(bindId);
        }
        return StrUtil.EMPTY;
    }

}
