package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * UserService层
 * 
 * @author xiezhiping
 *
 */
@Service
public class UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	@Autowired
	private UserDAO userDAO;

	@Autowired
	private LoginTicketDAO loginTicketDAO;

	/**
	 * 通过username和password注册，并进行一些判断和密码的加强
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public Map<String, Object> register(String username, String password) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.isBlank(username)) {
			map.put("msgname", "用户名不能为空");
			return map;
		}

		if (StringUtils.isBlank(password)) {
			map.put("msgpwd", "密码不能为空");
			return map;
		}

		// 查看user表中是否存在当前注册的用户名
		User user = userDAO.selectByName(username);

		if (user != null) {
			map.put("msgname", "用户名已经被注册");
			return map;
		}

		// 密码强度
		user = new User();
		user.setName(username);
		// 盐salt，主要是为了加强密码的强度，防止被MD5破解，在原始密码的基础上加上5为的随机salt，再进行MD5加密，即可以防止被网上已经破解出来的MD5序列串破解成功
		// 因为多了随机盐salt，则网上的破解库中不会存在该密码MD5的反破解密码，因此加强了密码的强度。当验证密码时再反编译MD5获得原始密码即可
		user.setSalt(UUID.randomUUID().toString().substring(0, 5));
		// 给用户随机分配一个图像
		String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
		user.setHeadUrl(head);
		user.setPassword(ToutiaoUtil.MD5(password + user.getSalt()));
		// 往user表中加入当前注册成功的用户
		userDAO.addUser(user);

		// 登陆
		String ticket = addLoginTicket(user.getId());
		map.put("ticket", ticket);
		return map;
	}

	/**
	 * 登陆
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public Map<String, Object> login(String username, String password) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.isBlank(username)) {
			map.put("msgname", "用户名不能为空");
			return map;
		}

		if (StringUtils.isBlank(password)) {
			map.put("msgpwd", "密码不能为空");
			return map;
		}

		// 根据输入的username在数据库表user中进行检索
		User user = userDAO.selectByName(username);

		if (user == null) {
			map.put("msgname", "用户名不存在");
			return map;
		}

		if (!ToutiaoUtil.MD5(password + user.getSalt()).equals(user.getPassword())) {
			map.put("msgpwd", "密码不正确");
			return map;
		}

		map.put("userId", user.getId());

		// 登陆时配置一个ticket
		String ticket = addLoginTicket(user.getId());
		map.put("ticket", ticket);
		return map;
	}

	/**
	 * 为登陆用户增加一个ticket，ticket用来标识一个用户的信息
	 * 
	 * @param userId
	 * @return
	 */
	private String addLoginTicket(int userId) {
		// 创建一个LoginTicket对象，存放LoginTicket对象的信息
		LoginTicket ticket = new LoginTicket();
		ticket.setUserId(userId);
		Date date = new Date();
		date.setTime(date.getTime() + 1000 * 3600 * 24);// 过期时间为1天
		ticket.setExpired(date);
		ticket.setStatus(0);// 0代表正常;1代表无效
		ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));// 为该用户随机配置一个ticketId
		loginTicketDAO.addTicket(ticket);
		return ticket.getTicket();
	}

	public User getUser(int id) {
		return userDAO.selectById(id);
	}

	/**
	 * 当输入/logout/时更新登陆的ticket状态为1，即status=1时表示为失效的用户，则退出登陆信息
	 * 
	 * @param ticket
	 */
	public void logout(String ticket) {
		//服务端/客户端的ticket状态置为失效，清除session
		loginTicketDAO.updateStatus(ticket, 1);
	}
}
