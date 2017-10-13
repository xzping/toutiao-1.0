package com.nowcoder.service;

import com.nowcoder.dao.NewsDAO;
import com.nowcoder.model.News;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.velocity.texen.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 新闻service层，调用NewsDAO层
 * 
 * @author xiezhiping
 *
 */
@Service
public class NewsService {
	@Autowired
	private NewsDAO newsDAO;

	/**
	 * 获取最新的新闻
	 * 
	 * @param userId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<News> getLatestNews(int userId, int offset, int limit) {
		return newsDAO.selectByUserIdAndOffset(userId, offset, limit);
	}

	/**
	 * 添加一则新闻资讯
	 * 
	 * @param news
	 * @return
	 */
	public int addNews(News news) {
		newsDAO.addNews(news);
		return news.getId();
	}

	/**
	 * 通过newsId获取指定新闻
	 * @param newsId
	 * @return
	 */
	public News getById(int newsId) {
		return newsDAO.getById(newsId);
	}

	/**
	 * 保存照片
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String saveImage(MultipartFile file) throws IOException {
		//判断文件是否为合法的，通过去判断文件的后缀名即可
		int dotPos = file.getOriginalFilename().lastIndexOf(".");
		if (dotPos < 0) {
			return null;
		}
		//读取文件的扩展名
		String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
		//判断后缀名是否合法
		if (!ToutiaoUtil.isFileAllowed(fileExt)) {
			return null;
		}

		String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;
		//将传进来的二进制文件file.getInputStream()，复制到本地存储地址中，如果本地存储地址已经存在这个文件名，则进行替换
		//public static long copy(InputStream in, Path target, CopyOption... options)
		Files.copy(file.getInputStream(), new File(ToutiaoUtil.IMAGE_DIR + fileName).toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		return ToutiaoUtil.TOUTIAO_DOMAIN + "image?name=" + fileName;
	}

	/**
	 * 更新新闻评论数目
	 * 
	 * @param id
	 * @param count
	 * @return
	 */
	public int updateCommentCount(int id, int count) {
		return newsDAO.updateCommentCount(id, count);
	}

	/**
	 * 更新点赞数目
	 * 
	 * @param id
	 * @param count
	 * @return
	 */
	public int updateLikeCount(int id, int count) {
		return newsDAO.updateLikeCount(id, count);
	}
}
