package com.nowcoder.util;

/**
 * redis工具类，redis存储的key的规范
 * LIKE:entityType:entityId
 * DISLIKE:entityType:entityId
 * 
 * @author xiezhiping
 *
 */
public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_LIKE = "LIKE";
    private static String BIZ_DISLIKE = "DISLIKE";
    private static String BIZ_EVENT = "EVENT";

    //返回事件的队列key
    public static String getEventQueueKey() {
        return BIZ_EVENT;
    }

    /**
     * entityId和entityType的组合可以表示喜欢资讯或者喜欢评论等（可复用）
     * 
     * @param entityId
     * @param entityType
     * @return
     */
    public static String getLikeKey(int entityId, int entityType) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getDisLikeKey(int entityId, int entityType) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }
}
