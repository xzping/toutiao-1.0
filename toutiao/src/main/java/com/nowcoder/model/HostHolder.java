package com.nowcoder.model;

import org.springframework.stereotype.Component;

/**
 * HostHolder类用途：用来存储用户，表示当前用户是谁，调用本地线程对象来获取当前登陆的用户信息（User对象）
 * 
 * @author xiezhiping
 *
 */
// @Component注解的作用是，把普通pojo实例化到spring容器中，相当于配置文件中的<bean id="" class=""/>
@Component
public class HostHolder {
	// 服务器不是一个人在用，通过线程本地变量来存储当前用户自己的信息，多线程之间互相不冲突。
	private static ThreadLocal<User> users = new ThreadLocal<User>();

	public User getUser() {
		return users.get();
	}

	public void setUser(User user) {
		users.set(user);
	}

	public void clear() {
		users.remove();
	}
}
