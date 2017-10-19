package com.nowcoder.asyncTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.fastjson.JSON;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;

public class EventConsumer implements InitializingBean, ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
	private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();
	private ApplicationContext applicationContext;

	@Autowired
	JedisAdapter jedisAdapter;

	// 初始化bean
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
		if (beans != null) {
			for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
				// 把所有注册的的handler是啊金放到list集合中
				List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
				// 把这个事件所关联的事件加入
				for (EventType type : eventTypes) {
					if (!config.containsKey(type)) {
						config.put(type, new ArrayList<EventHandler>());
					}
					config.get(type).add(entry.getValue());
				}
			}
		}

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// 不断从队列中取出数据
				while (true) {
					// 取出数据
					String key = RedisKeyUtil.getEventQueueKey();// key="EVENT"
					// 把key对应的事件取出来
					List<String> events = jedisAdapter.brpop(0, key);
					// 遍历取出来的事件
					for (String message : events) {
						if (message.equals(key)) {
							continue;
						}

						// 将取出来的事件转换为EventModel
						EventModel eventModel = JSON.parseObject(message, EventModel.class);
						// 若为未注册过的事件，则抛掉
						if (!config.containsKey(eventModel.getType())) {
							logger.error("不能识别的事件");
							continue;
						}

						// 找到handler去处理相应的事件
						for (EventHandler handler : config.get(eventModel.getType())) {
							handler.doHandle(eventModel);
						}
					}
				}
			}
		});
		thread.start();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		this.applicationContext = applicationContext;
	}
}
