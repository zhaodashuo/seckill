package zhaoshuo.seckill.Aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import zhaoshuo.seckill.redis.lock.RedissonDistributedLocker;
import zhaoshuo.seckill.response.SeckillInfoResponse;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-05-03 13:31
 */

@Component
@Order(1)
@Aspect
public class Aop {
    @Autowired
    private RedissonDistributedLocker redissonDistributedLocker;

    @Pointcut("@annotation(zhaoshuo.seckill.Aop.ServiceLock)")
    public void lockAspect(){

    }

    @Around("lockAspect()")
    public SeckillInfoResponse around(ProceedingJoinPoint joinPoint){

        Object[] args = joinPoint.getArgs();
        int stallActivityId=(Integer)args[0];
        String lockKey = "BM_MARKET_SECKILL_" + stallActivityId;
        redissonDistributedLocker.lock(lockKey, 2L);
        Object proceed=null;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            redissonDistributedLocker.unlock(lockKey);
        }
        return (SeckillInfoResponse)proceed;
    }


}
