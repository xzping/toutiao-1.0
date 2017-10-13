package com.nowcoder.controller;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.News;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实现登陆和注册的接口
 * 
 * @author xiezhiping
 *
 */
@Controller
public class LoginController {
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	UserService userService;

	@Autowired
	EventProducer eventProducer;

	/**
	 * 注册接口。后端把返回到前端的数据存储在model中，而前端通过@RequestParam注解从前端请求页面中传递参数到后端
	 * 注册窗口中有用户名、密码、记住我三个字段值，将这三个字段值分别传到后端来处理
	 * 
	 * @param model
	 * @param username
	 * @param password
	 * @param rememberme
	 *            0代表不记住，1代笔记住
	 * @param response
	 * @return
	 */
	@RequestMapping(path = { "/reg/" }, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String reg(Model model, @RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam(value = "rember", defaultValue = "0") int rememberme, HttpServletResponse response) {
		try {
			Map<String, Object> map = userService.register(username, password);
			if (map.containsKey("ticket")) {
				Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
				cookie.setPath("/");
				if (rememberme > 0) {
					cookie.setMaxAge(3600 * 24 * 5);
				}
				response.addCookie(cookie);
				return ToutiaoUtil.getJSONString(0, "注册成功");
			} else {
				return ToutiaoUtil.getJSONString(1, map);
			}

		} catch (Exception e) {
			logger.error("注册异常" + e.getMessage());
			return ToutiaoUtil.getJSONString(1, "注册异常");
		}
	}

	@RequestMapping(path = { "/login/" }, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String login(Model model, @RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam(value = "rember", defaultValue = "0") int rememberme, HttpServletResponse response) {
		try {
			//把当前登陆用户的信息调取出来，存放到map集合中
			Map<String, Object> map = userService.login(username, password);
			//进行ticket登记，把当前用户的ticket存放在cookie中
			if (map.containsKey("ticket")) {
				Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
				cookie.setPath("/");
				//设置cookie的时间为5天
				if (rememberme > 0) {
					cookie.setMaxAge(3600 * 24 * 5);
				}
				//向http相应中加入该用户的ticket信息
				response.addCookie(cookie);
				eventProducer.fireEvent(new EventModel(EventType.LOGIN).setActorId((int) map.get("userId"))
						.setExt("username", username).setExt("email", "zjuyxy@qq.com"));
				return ToutiaoUtil.getJSONString(0, "成功");
			} else {
				return ToutiaoUtil.getJSONString(1, map);
			}

		} catch (Exception e) {
			logger.error("注册异常" + e.getMessage());
			return ToutiaoUtil.getJSONString(1, "注册异常");
		}
	}

	/**
	 * 登出：前端把当前用户的ticket传进来，把当前用户的ticket清理掉，也就是把ticket的状态置为1，为无效用户
	 * 
	 * @param ticket
	 * @return
	 */
	@RequestMapping(path = { "/logout/" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String logout(@CookieValue("ticket") String ticket) {
		userService.logout(ticket);
		// 登出以后自动跳转到首页，也就是重定向redirect:/
		return "redirect:/";
	}

}
