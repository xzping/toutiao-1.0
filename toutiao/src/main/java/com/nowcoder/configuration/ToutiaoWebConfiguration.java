package com.nowcoder.configuration;

import com.nowcoder.interceptor.LoginRequiredInterceptor;
import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 注册拦截器,初始化拦截器配置
 * 
 * @author xiezhiping
 *
 */
@Component
public class ToutiaoWebConfiguration extends WebMvcConfigurerAdapter {
	@Autowired
	PassportInterceptor passportInterceptor;

	@Autowired
	LoginRequiredInterceptor loginRequiredInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 首先，注册passportInterceptor拦截器，即每次进行controller层时，就会回调passportInterceptor拦截器
		registry.addInterceptor(passportInterceptor);
		// 接着，注册loginRequiredInterceptor拦截器
		registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/msg/*").addPathPatterns("/like")
				.addPathPatterns("/dislike");
		super.addInterceptors(registry);
	}
}
