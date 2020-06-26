package zhaoshuo.seckill.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.*;

import zhaoshuo.seckill.Interceptor.LimitInterceptor;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-24 13:13
 */
@Configuration
@Component
public class WebConfig implements WebMvcConfigurer {

	public void addInterceptors(InterceptorRegistry registry) {
		//多个拦截器组成一个拦截器链
		registry.addInterceptor(new LimitInterceptor(1000, LimitInterceptor.LimitType.DROP)).addPathPatterns("/api/seckill/goSeckillByQueue");
		//super.addInterceptors(registry);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/static/")
				.addResourceLocations("classpath:/static/templates/");

		//super.addResourceHandlers(registry);
	}
}
