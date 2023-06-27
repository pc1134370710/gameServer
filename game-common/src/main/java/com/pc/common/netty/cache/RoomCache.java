package com.pc.common.netty.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pc.common.netty.model.RoomServer;

/**
 * @author Kevin
 * @className: RoomCache
 * @description: 服务器房间缓存
 * @date 2023/6/20 13:56
 */
public class RoomCache {

    /**
     * 房间
     */
    private static final Cache<String, RoomServer> roomServerMap = Caffeine.newBuilder().build();

    public static void put(String key, RoomServer roomServer) {
        roomServerMap.put(key, roomServer);
    }

    public static RoomServer get(String key) {
        return roomServerMap.getIfPresent(key);
    }

    public static Cache<String, RoomServer> all() {
        return roomServerMap;
    }

}
