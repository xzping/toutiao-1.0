package com.nowcoder.asyncTest;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;

public class EventProducer {
	@Autowired
    JedisAdapter jedisAdapter;
	
	public boolean fireEvent(EventModel model) {
		try {
			String json=JSONObject.toJSONString(model);
			String key=RedisKeyUtil.getEventQueueKey();//key="EVENT"
			jedisAdapter.lpush(key, json);
			return true;
		}catch (Exception e) {
            return false;
        }
	}
}
