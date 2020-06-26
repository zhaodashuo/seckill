package zhaoshuo.seckill.Interceptor;


import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import zhaoshuo.seckill.Exception.WebException;
import zhaoshuo.seckill.Exception.WebExceptionEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**

 * Guava RateLimiter
 * 1、实现限流器进行限流
 * 2、使用kafka实现解耦和削峰
 * 3、配置动态数据源，实现读写分离
 * 4、Redission实现分布式锁，保证商品不超卖
 * 5、jps实现持久化
 * 6、通过将大部分的业务逻辑搬移到缓冲中处理，最大程度减小数据库的io操作。
 */
public class LimitInterceptor extends HandlerInterceptorAdapter {
	private Logger logger=LoggerFactory.getLogger(LimitInterceptor.class);
	public enum LimitType {
		DROP,	//丢弃
		WAIT	//等待
	}
	
	/**
	 * 限流器
	 */
	private RateLimiter limiter;
	
	/**
	 * 限流方式
	 */
	private LimitType limitType = LimitType.DROP;
	
	public LimitInterceptor() {
		this.limiter = RateLimiter.create(1);
	}
	
	/**
	 * @param tps	限流（每秒处理量）
	 * @param limitType
	 */
	public LimitInterceptor(int tps, LimitInterceptor.LimitType limitType) {
		this.limiter = RateLimiter.create(tps);
		this.limitType = limitType;
	}

	/**
	 * @param permitsPerSecond	每秒新增的令牌数
	 * @param limitType	限流类型
	 */
	public LimitInterceptor(double permitsPerSecond, LimitInterceptor.LimitType limitType) {
		this.limiter = RateLimiter.create(permitsPerSecond, 10, TimeUnit.MILLISECONDS);
		this.limitType = limitType;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	    if (limitType.equals(LimitType.DROP)) {
		    if (limiter.tryAcquire()) {
		    	logger.info("限流器工作");
		        return super.preHandle(request, response, handler);
		    }
	    } else {
	    	limiter.acquire();
	    	return super.preHandle(request, response, handler);
	    }
	    throw new WebException(WebExceptionEnum.REQUEST_LIMIT);//达到限流后，往页面提示的错误信息。
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}
	
	public RateLimiter getLimiter() {
	    return limiter;
	}
	 
	public void setLimiter(RateLimiter limiter) {
	    this.limiter = limiter;
	}

}
