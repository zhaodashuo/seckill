package zhaoshuo.seckill.Aop;

import java.lang.annotation.*;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-05-03 13:35
 */
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceLock {
}
