package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * passportInterceptor拦截器的功能判断是否为登陆用户
 * 即是在执行controller之前，首先获取当前用户是否为登陆过的用户（在过期日期之前），如果为登陆过的用户，则自动获取该用户的ticket信息
 * 
 * @author xiezhiping
 *
 */
@Component
public class PassportInterceptor implements HandlerInterceptor {

	// 用户的页面访问时，传过来的是ticket信息，ticket是存储在cookie中的
	@Autowired
	private LoginTicketDAO loginTicketDAO;

	// 访问用户
	@Autowired
	private UserDAO userDAO;

	@Autowired
	private HostHolder hostHolder;

	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest,
							HttpServletResponse httpServletResponse, Object o)
			throws Exception {
		String ticket = null;
		// 遍历http请求中的cookie中有没有该ticket信息
		if (httpServletRequest.getCookies() != null) {
			for (Cookie cookie : httpServletRequest.getCookies()) {
				if (cookie.getName().equals("ticket")) {
					ticket = cookie.getValue();
					break;
				}
			}
		}

		// 如果ticket不为null，则说明为登陆用户（过期时间之内）
		if (ticket != null) {
			LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
			if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) {
				return true;
			}

			// 通过ticket知道了访问的用户
			User user = userDAO.selectById(loginTicket.getUserId());
			// 把当前用户设置为访问用户
			hostHolder.setUser(user);
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
			ModelAndView modelAndView) throws Exception {
		// 结束之后，即controller层运行结束，如果modelAndView不是空的，则进行渲染
		if (modelAndView != null && hostHolder.getUser() != null) {
			modelAndView.addObject("user", hostHolder.getUser());
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Object o, Exception e) throws Exception {
		hostHolder.clear();// 把当前用户清掉
	}
}
