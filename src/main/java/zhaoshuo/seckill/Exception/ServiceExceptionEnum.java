package zhaoshuo.seckill.Exception;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-24 13:13
 */
public interface ServiceExceptionEnum {
    
    /**
     * 请求是否成功
     */
    Boolean getIsSuccess();
    
    /**
     * 获取返回的code
     */
    Integer getResponseCode();
    
    /**
     * 获取返回的message
     */
    String getResponseMsg();
}
