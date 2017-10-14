package com.nowcoder.controller;

import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.News;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页接口层
 * 主页实现功能：获取新闻列表、读取某个用户的历史新闻资讯
 * 
 * @author xiezhiping
 *
 */
@Controller
public class HomeController {
	//@Autowired实现从容器中自动注入
	@Autowired
	NewsService newsService;

	@Autowired
	UserService userService;

	@Autowired
	LikeService likeService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	MailSender mailSender;

	private List<ViewObject> getNews(int userId, int offset, int limit) {
		// 通过userId来获取最新的新闻列表，并放入到List集合中
		List<News> newsList = newsService.getLatestNews(userId, offset, limit);
		// 获取当前的登陆用户的信息，如果是登陆用户，则localUserId为登陆用户的userId，否则为0
		int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
		// 创建一个List集合，每一个List集合对象存放着ViewObject对象，即一个map集合，map集合存放着用户或者新闻获取其他的若干的信息
		List<ViewObject> vos = new ArrayList<>();
		// 遍历List集合，将每一条新闻咨询都存放在相应的ViewObject对象中
		for (News news : newsList) {
			ViewObject vo = new ViewObject();
			vo.set("news", news);
			vo.set("user", userService.getUser(news.getUserId()));
			//如果当前为登陆状态，则可以看到当前新闻资讯的点赞状态，否则状态为0
			if (localUserId != 0) {
				vo.set("like", likeService.getLikeStatus(localUserId, EntityType.ENTITY_NEWS, news.getId()));
			} else {
				vo.set("like",0);
			}
			vos.add(vo);
		}
		return vos;
	}

	/**
	 * 主页显示的接口，进入主页的url可以使用/或者/index
	 * 仅使用@RequestMapping注解，而没有使用@ResponseBody注解时，返回的是一个模版（ModelAndView）
	 * 若使用了@ResponseBody注解，则返回的为相应的主体信息，一般为JSON串
	 * 
	 * @param model
	 * @param pop
	 * @return
	 */
	@RequestMapping(path = { "/", "/index" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String index(Model model, @RequestParam(value = "pop", defaultValue = "0") int pop) {
		// SpringMVC通过一个Model来前后端传递数据，将要传递给前端的数据传入到model对象中即可，是一个key-value的对象，
		// 前端通过调用model对象的key便可以读取到后端发送的数据信息
		// 当userId=0时，显示所有的新闻资讯
		model.addAttribute("vos", getNews(0, 0, 20));

		// LoginPop邮箱登陆
		if (hostHolder.getUser() != null) {
			pop = 0;
		}
		model.addAttribute("pop", pop);

		return "home";
	}

	// 点击用户头像，则转入到该用户的个人主页中，即可以看到该用户发过的历史资讯
	@RequestMapping(path = { "/user/{userId}" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String userIndex(Model model, @PathVariable("userId") int userId) {
		// 获取该用户的10条历史资讯
		model.addAttribute("vos", getNews(userId, 0, 10));
		// 因为个人页面和主页的显示是一样的，只是进行了过滤，只显示当前userId的新闻资讯
		return "home";
	}

}
