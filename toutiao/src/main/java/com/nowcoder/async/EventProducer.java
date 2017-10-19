package com.nowcoder.async;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事件的发起者，把事件放进一个队列里面
 * 
 * @author xiezhiping
 *
 */
@Service
public class EventProducer {
	//事件的队列，通过redis来实现这个队列
    @Autowired
    JedisAdapter jedisAdapter;

    //把事件放到队列里面
    public boolean fireEvent(EventModel model) {
        try {
            String json = JSONObject.toJSONString(model);
            String key = RedisKeyUtil.getEventQueueKey();
            //把事件信息放到队列中
            jedisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
