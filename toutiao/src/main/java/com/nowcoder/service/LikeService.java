package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 点赞点踩的service层
 * 
 * @author xiezhiping
 *
 */
@Service
public class LikeService {
	
    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 判断某个用户对某资讯/评论是否喜欢
     * 如果喜欢则返回1，不喜欢返回-1，否则返回0
     * 
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int getLikeStatus(int userId, int entityType, int entityId) {
        //获取对这个资讯/评论喜欢的key
    		String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
    		//判断这个集合中是否有这个喜欢的key，有则返回1
        if(jedisAdapter.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        //判断这个dislike的key，有则返回-1
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        return jedisAdapter.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;
    }

    /**
     * like操作，返回的是集合中的相应点赞的数量
     * 
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public long like(int userId, int entityType, int entityId) {
        // 在喜欢集合里增加
        String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));//加入set集合sadd
        // 从反对里删除
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        jedisAdapter.srem(disLikeKey, String.valueOf(userId));//从set集合移除
        //重新查找当前的点赞数，防止多线程同时操作
        return jedisAdapter.scard(likeKey);
    }

    /**
     * dislike操作
     * 
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public long disLike(int userId, int entityType, int entityId) {
        // 在反对集合里增加
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityId, entityType);
        jedisAdapter.sadd(disLikeKey, String.valueOf(userId));
        // 从喜欢里删除
        String likeKey = RedisKeyUtil.getLikeKey(entityId, entityType);
        jedisAdapter.srem(likeKey, String.valueOf(userId));
        return jedisAdapter.scard(likeKey);
    }
}
