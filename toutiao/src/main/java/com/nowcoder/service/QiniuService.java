package com.nowcoder.service;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.ToutiaoUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

/**
 * 七牛云存储service层
 * 
 * @author xiezhiping
 *
 */
@Service
public class QiniuService {
	/*
	 * 使用七牛云CDN存储：实现存储分离。云存储，指图片的同步，没有使用云存储时，若用户1上传了一张图片，
	 * 他得通知到其他的用户他这张图片存储在本地的哪个位置上， 当其他用户读取用户1的这张照片时，需要寻找到用户1的图片本地存储位置，并获取图片。
	 * 
	 * 当使用云存储CDN时，会让图片的访问速度加快，CDN的分布结构有源站和若干CDN节点，
	 * 当用户访问某一张照片时，不用直接访问源站，而是找到离该用户最近的一个CDN节点进行访问静态文件。 CDN源站把这些静态文件都分发到CDN节点上。
	 */

	private static final Logger logger = LoggerFactory.getLogger(QiniuService.class);
	// 设置好账号的ACCESS_KEY和SECRET_KEY
	String ACCESS_KEY = "gGFf-cA7cy-hyhN3jwTByS7h8SSDuGMtyKFZIIVf";
	String SECRET_KEY = "7X51IlDOMO6UjQfL_vDcXPIr2PnLOickH1F2m30u";
	// 要上传的空间
	String bucketname = "nowcoder";

	// 密钥配置
	Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
	// 构造一个带指定Zone对象的配置类
	Configuration cfg = new Configuration(Zone.zone2());
	// ...其他参数参考类注释
	UploadManager uploadManager = new UploadManager(cfg);

	private static String QINIU_IMAGE_DOMAIN = "http://ox6xu9hb7.bkt.clouddn.com/";

	// 简单上传，使用默认策略，只需要设置上传的空间名就可以了
	public String getUpToken() {
		return auth.uploadToken(bucketname);
	}

	public String saveImage(MultipartFile file) throws IOException {
		try {
			int dotPos = file.getOriginalFilename().lastIndexOf(".");
			if (dotPos < 0) {
				return null;
			}
			String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
			if (!ToutiaoUtil.isFileAllowed(fileExt)) {
				return null;
			}

			String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;
			// 调用put方法上传
			Response res = uploadManager.put(file.getBytes(), fileName, getUpToken());
			// 打印返回的信息
			if (res.isOK() && res.isJson()) {
				//返回这张存储照片的地址http://ox6xu9hb7.bkt.clouddn.com/f07ff5a8945b4d6fb1bfc4cc8910e0fb.png，json串格式为：{"msg":"http://ox6xu9hb7.bkt.clouddn.com/f07ff5a8945b4d6fb1bfc4cc8910e0fb.png","code":0}
				return QINIU_IMAGE_DOMAIN + JSONObject.parseObject(res.bodyString()).get("key");
			} else {
				logger.error("七牛异常:" + res.bodyString());
				return null;
			}
		} catch (QiniuException e) {
			// 请求失败时打印的异常的信息
			logger.error("七牛异常:" + e.getMessage());
			return null;
		}
	}
}
