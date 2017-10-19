package com.nowcoder.asyncTest.handler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.nowcoder.asyncTest.EventHandler;
import com.nowcoder.asyncTest.EventModel;
import com.nowcoder.asyncTest.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;

public class LikeHandler implements EventHandler {

	@Autowired
	MessageService messageService;

	@Autowired
	UserService userService;

	@Override
	public void doHandle(EventModel model) {
		Message message = new Message();
		message.setFromId(3);
		message.setToId(model.getEntityOwnerId());
		User user = userService.getUser(model.getActorId());
		message.setContent("用户" + user.getName() + "赞了你的资讯,http://127.0.0.1:8080/news/" + model.getEntityId());
		message.setCreatedDate(new Date());
		messageService.addMessage(message);
	}

	@Override
	public List<EventType> getSupportEventTypes() {
		return Arrays.asList(EventType.LIKE);
	}

}
