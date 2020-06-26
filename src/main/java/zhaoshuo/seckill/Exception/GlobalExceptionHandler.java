package zhaoshuo.seckill.Exception;


import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import zhaoshuo.seckill.response.ResponseBean;

import javax.servlet.http.HttpServletRequest;


/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-24 13:13
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@ExceptionHandler(value = AssertException.class)
    @ResponseBody
    public ResponseBean BootExceptionHandler(HttpServletRequest req, AssertException e) {
		ResponseBean response = new ResponseBean(false, e.getCode(), e.getMessage(), null);
    	logger.error(e.getCode()+"",e);
    	logger.warn("ExceptionHandler:"+response.toString());
        return response;
    }
	
	@ExceptionHandler(value = JSONException.class)
	@ResponseBody
	public ResponseBean JSONExceptionHandler(HttpServletRequest req, JSONException e) {		
		ResponseBean response = new ResponseBean(false, 1102, "请求参数格式异常", null);
		logger.error("1102",e);
		logger.error("ExceptionHandler"+response.toString());
		return response;
	}
	
	@ExceptionHandler(value = JsonParseException.class)
	@ResponseBody
	public ResponseBean JsonParseExceptionHandler(HttpServletRequest req, JsonParseException e) {
		ResponseBean response = new ResponseBean(false, 1102, "请求参数格式异常", null);
		logger.error("1102",e);
		logger.error("ExceptionHandler"+response.toString());
		return response;
	}
	
	@ExceptionHandler(value = WebException.class)
    @ResponseBody
    public ResponseBean ExceptionHandler(HttpServletRequest req, WebException e) {   	
		ResponseBean response = new ResponseBean(e.getIsSuccess(), e.getResponseCode(), e.getResponseMsg(), null);
		logger.error(e.getResponseCode()+"",e);
    	logger.error("ExceptionHandler"+response.toString());
    	return response;
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseBean ExceptionHandler(HttpServletRequest req, Exception e) {   	
		ResponseBean response = new ResponseBean(false, 1000, "服务器正在繁忙，请稍后再试哦~", null);
		logger.error("1000",e);
    	logger.error("ExceptionHandler"+response.toString());
    	return response;
    }

}
