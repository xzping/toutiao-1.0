package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.ToutiaoUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 发布新闻资讯接口
 * 
 * @author xiezhiping
 *
 */
@Controller
public class NewsController {
	private static final Logger logger = LoggerFactory.getLogger(NewsController.class);
	@Autowired
	NewsService newsService;

	@Autowired
	QiniuService qiniuService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	UserService userService;

	@Autowired
	CommentService commentService;

	@Autowired
	LikeService likeService;

	/**
	 * 读取指定newsId的的新闻资讯，即资讯详情页
	 * 
	 * @param newsId
	 * @param model
	 * @return
	 */
	@RequestMapping(path = { "/news/{newsId}" }, method = { RequestMethod.GET })
	public String newsDetail(@PathVariable("newsId") int newsId, Model model) {
		// 取出当前newsId的这条资讯
		News news = newsService.getById(newsId);
		if (news != null) {
			int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
			if (localUserId != 0) {
				model.addAttribute("like",
						likeService.getLikeStatus(localUserId, EntityType.ENTITY_NEWS, news.getId()));
			} else {
				model.addAttribute("like", 0);
			}
			// 评论
			List<Comment> comments = commentService.getCommentsByEntity(news.getId(), EntityType.ENTITY_NEWS);// 对资讯进行评论
			// 对评论进行遍历
			List<ViewObject> commentVOs = new ArrayList<ViewObject>();
			for (Comment comment : comments) {
				ViewObject vo = new ViewObject();
				vo.set("comment", comment);
				vo.set("user", userService.getUser(comment.getUserId()));
				commentVOs.add(vo);
			}
			model.addAttribute("comments", commentVOs);
		}
		// 返回前端当前资讯，为了让前端调用$!{news.title}、$!{news.commentCount}等字段显示
		model.addAttribute("news", news);
		// 资讯的作者，为了让前端调用$!{owner.id}、$!{owner.name}等字段显示
		model.addAttribute("owner", userService.getUser(news.getUserId()));
		return "detail";
	}

	/**
	 * 增加新闻资讯的评论，传入新闻id的newsId和评论的内容content
	 * 
	 * @param newsId
	 * @param content
	 * @return
	 */
	@RequestMapping(path = { "/addComment" }, method = { RequestMethod.POST })
	public String addComment(@RequestParam("newsId") int newsId, @RequestParam("content") String content) {
		try {
			content = HtmlUtils.htmlEscape(content);
			// 过滤content
			Comment comment = new Comment();
			comment.setUserId(hostHolder.getUser().getId());
			comment.setContent(content);
			comment.setEntityId(newsId);
			comment.setEntityType(EntityType.ENTITY_NEWS);
			comment.setCreatedDate(new Date());
			comment.setStatus(0);

			commentService.addComment(comment);
			// 更新news里的评论数量
			int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
			newsService.updateCommentCount(comment.getEntityId(), count);
			// 怎么异步化
		} catch (Exception e) {
			logger.error("增加评论失败" + e.getMessage());
		}
		return "redirect:/news/" + String.valueOf(newsId);
	}

	/**
	 * 获取图片信息,展示 通过HttpServletResponse返回图片信息
	 * 
	 * @param imageName
	 * @param response
	 */
	@RequestMapping(path = { "/image" }, method = { RequestMethod.GET })
	@ResponseBody
	public void getImage(@RequestParam("name") String imageName, HttpServletResponse response) {
		try {
			// 文件类型
			response.setContentType("image/jpeg");
			// 将原有的图片在本地存储目录下copy到response.getOutputStream()中
			StreamUtils.copy(new FileInputStream(new File(ToutiaoUtil.IMAGE_DIR + imageName)),
					response.getOutputStream());
		} catch (Exception e) {
			logger.error("读取图片错误" + imageName + "错误信息为" + e.getMessage());
		}
	}

	/**
	 * 上传图片按钮
	 * 
	 * @param file
	 * @return
	 */
	@RequestMapping(path = { "/uploadImage/" }, method = { RequestMethod.POST })
	@ResponseBody
	public String uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			// 使用本地存储图片
			// String fileUrl = newsService.saveImage(file);

			// 使用七牛云存储CDN
			String fileUrl = qiniuService.saveImage(file);
			if (fileUrl == null) {
				return ToutiaoUtil.getJSONString(1, "上传图片失败");
			}
			return ToutiaoUtil.getJSONString(0, fileUrl);
		} catch (Exception e) {
			logger.error("上传图片失败" + e.getMessage());
			return ToutiaoUtil.getJSONString(1, "上传失败");
		}
	}

	/**
	 * 分享资讯按钮 前端传入三个参数，图片、标题和链接
	 * 
	 * @param image
	 * @param title
	 * @param link
	 * @return
	 */
	@RequestMapping(path = { "/user/addNews/" }, method = { RequestMethod.POST })
	@ResponseBody
	public String addNews(@RequestParam("image") String image, @RequestParam("title") String title,
			@RequestParam("link") String link) {
		try {
			// 构造一个news对象，用来存放前端传入的新闻资讯信息等字段
			News news = new News();
			news.setCreatedDate(new Date());// 创建时间
			news.setTitle(title);// 标题
			news.setImage(image);// 图片
			news.setLink(link);// 链接
			// 如果当前用户已经登陆，则将当前用户的userId传入到news表字段中，主外键关联
			if (hostHolder.getUser() != null) {
				news.setUserId(hostHolder.getUser().getId());
			} else {
				// 设置一个匿名用户，匿名分享
				news.setUserId(3);
			}
			// 将当前分享的新闻资讯添加到数据库中
			newsService.addNews(news);
			return ToutiaoUtil.getJSONString(0);
		} catch (Exception e) {
			logger.error("添加资讯失败" + e.getMessage());
			return ToutiaoUtil.getJSONString(1, "发布失败");
		}
	}
}
