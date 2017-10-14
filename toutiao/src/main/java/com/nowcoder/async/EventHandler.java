package com.nowcoder.async;

import java.util.List;

/**
 * 事件处理的接口
 * 
 * @author xiezhiping
 *
 */
public interface EventHandler {
	//事件处理
    void doHandle(EventModel model);
    //关注哪一些事件
    List<EventType> getSupportEventTypes();
}
