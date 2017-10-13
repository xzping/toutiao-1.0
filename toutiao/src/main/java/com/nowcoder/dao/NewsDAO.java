package com.nowcoder.dao;

import com.nowcoder.model.News;
import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * NewsDAO层，负责底层调用数据库数据 为一个接口，负责增删改查等方法的定义和mybatis注释。接口使用@Mapper进行注解。
 * 
 * @author xiezhiping
 *
 */
@Mapper
public interface NewsDAO {
	// 调用的数据库表名为news表
	String TABLE_NAME = "news";
	// 插入的字段值
	String INSERT_FIELDS = " title, link, image, like_count, comment_count, created_date, user_id ";
	// 选择的字段值
	String SELECT_FIELDS = " id, " + INSERT_FIELDS;

	/*
	 * 使用注解的myBatis，预编译是使用#{}传入参数值
	 */
	@Insert({ "insert into ", TABLE_NAME, "(", INSERT_FIELDS,
			") values (#{title},#{link},#{image},#{likeCount},#{commentCount},#{createdDate},#{userId})" })
	int addNews(News news);// 获取新闻列表

	@Select({ "select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}" })
	News getById(int id); // 通过id来获取新闻

	@Update({ "update ", TABLE_NAME, " set comment_count = #{commentCount} where id=#{id}" })
	int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);// 更新评论数量

	@Update({ "update ", TABLE_NAME, " set like_count = #{likeCount} where id=#{id}" })
	int updateLikeCount(@Param("id") int id, @Param("likeCount") int likeCount);// 更新点赞数

	/*
	 * 如果是复杂的sql语句，可以用xml文件进行编写，在resources目录下创建与该DAO文件名一样的xml文件，
	 * 且xml的namespace要与该DAO文件名一致。可以向该xml文件中传入一些参数，userId、offset和limit，
	 * 这些传入参数在xml文件中也用预编译的方式进行，即#{userId}、#{offset}和#{limit}
	 */
	List<News> selectByUserIdAndOffset(@Param("userId") int userId, @Param("offset") int offset,
			@Param("limit") int limit);// 通过user_id查找，分页获取新闻
}
