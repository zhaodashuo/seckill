/**  
* <p>Title: SeckillServiceImpl.java</p>  
* <p>Description: </p>  
* <p>Copyright: Copyright (c) 2018</p>  
* <p>Company: www.bluemoon.com</p>  
* @author Guoqing  
* @date 2018年8月10日  
*/  
package zhaoshuo.seckill.Service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import zhaoshuo.seckill.Aop.ServiceLock;
import zhaoshuo.seckill.Entity.SuccessKilled;
import zhaoshuo.seckill.JpaQuery.DynamicQuery;
import zhaoshuo.seckill.redis.lock.RedissonDistributedLocker;
import zhaoshuo.seckill.redis.repository.RedisRepository;
import zhaoshuo.seckill.Service.ISeckillService;
import zhaoshuo.seckill.kafka.KafkaSender;
import zhaoshuo.seckill.response.BaseResponse;
import zhaoshuo.seckill.response.SeckillInfoResponse;


import java.sql.Timestamp;
import java.util.Date;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-23 13:35
 */
@Service
public class SeckillServiceImpl implements ISeckillService {

//	@Autowired
//	Xiadan xiadan;

	@Autowired
	private DynamicQuery dynamicQuery;

	@Autowired
	private RedisRepository redisRepository;
	@Autowired
	private KafkaSender kafkaSender;
	@Autowired
	private RedissonDistributedLocker redissonDistributedLocker;
	
	private Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);
	@ServiceLock
	@Override
	@Transactional
	public SeckillInfoResponse startSeckill(int stallActivityId, int purchaseNum, String openId, String formId, long addressId,
											String shareCode, String shareSource, String userCode) {
		SeckillInfoResponse response = new SeckillInfoResponse();
		//判断秒杀活动是否开始
		if( !checkStartSeckill(stallActivityId) ) {
			response.setIsSuccess(false);
			response.setResponseCode(6205);
			response.setResponseMsg("秒杀活动尚未开始，请稍等！");
			response.setRefreshTime(0);
			return response;
		}
		logger.info("开始获取锁资源...");
		String lockKey = "BM_MARKET_SECKILL_" + stallActivityId;
		try {
			redissonDistributedLocker.lock(lockKey, 2L);
			logger.info("获取到锁资源...");
			//做用户重复购买校验
			if( redisRepository.exists("BM_MARKET_SECKILL_LIMIT_" + stallActivityId + "_" + openId) ) {
				logger.info("已经检测到用户重复购买...");
				response.setIsSuccess(false);
				response.setResponseCode(6105);
				response.setResponseMsg("您正在参与该活动，不能重复购买");
				response.setRefreshTime(0);
			} else {
				String redisStock = redisRepository.get("BM_MARKET_SECKILL_STOCKNUM_" + stallActivityId);
				int surplusStock = Integer.parseInt(redisStock == null ? "0" : redisStock);	//剩余库存
				//如果剩余库存大于购买数量，则进入消费队列
				if( surplusStock >= purchaseNum ) {
					try {
						//锁定库存，并将请求放入消费队列
						redisRepository.decrBy("BM_MARKET_SECKILL_STOCKNUM_" + stallActivityId, purchaseNum);
						JSONObject jsonStr = new JSONObject();
						jsonStr.put("stallActivityId", stallActivityId);
						jsonStr.put("purchaseNum", purchaseNum);
						jsonStr.put("openId", openId);
						jsonStr.put("addressId", addressId);
						jsonStr.put("formId", formId);
						jsonStr.put("shareCode", shareCode);
						jsonStr.put("shareSource", shareSource);
						jsonStr.put("userCode", userCode);
						//放入kafka消息队列
						kafkaSender.sendChannelMess("demo_seckill", jsonStr.toString());
//						messageQueueService.sendMessage("bm_market_seckill", jsonStr.toString(), true);
						//此处还应该标记一个seckillId和openId的唯一标志来给轮询接口判断请求是否已经处理完成，需要在下单完成之后去维护删除该标志，并且创建一个新的标志，并存放orderId
						redisRepository.set("BM_MARKET_LOCK_POLLING_" + stallActivityId + "_" + openId, "true");
						//维护一个key，防止用户在该活动重复购买，当支付过期之后应该维护删除该标志
						redisRepository.setExpire("BM_MARKET_SECKILL_LIMIT_" + stallActivityId + "_" + openId, "true", 3600*24*7);
						
						response.setIsSuccess(true);
						response.setResponseCode(6101);
						response.setResponseMsg("排队中，请稍后");
						response.setRefreshTime(1000);
					} catch (Exception e) {
						e.printStackTrace();
						response.setIsSuccess(false);
						response.setResponseCode(6102);
						response.setResponseMsg("秒杀失败，商品已经售罄");
						response.setRefreshTime(0);
					}
				}else {
					//需要在消费端维护一个真实的库存损耗值，用来显示是否还有未完成支付的用户
					String redisRealStock = redisRepository.get("BM_MARKET_SECKILL_REAL_STOCKNUM_" + stallActivityId);
					int realStock = Integer.parseInt(redisRealStock == null ? "0" : redisRealStock);	//剩余的真实库存 
					if( realStock > 0 ) {
						response.setIsSuccess(false);
						response.setResponseCode(6103);
						response.setResponseMsg("秒杀失败，还有部分订单未完成支付，超时将返还库存");
						response.setRefreshTime(0);
					} else {
						response.setIsSuccess(false);
						response.setResponseCode(6102);
						response.setResponseMsg("秒杀失败，商品已经售罄");
						response.setRefreshTime(0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setIsSuccess(false);
			response.setResponseCode(6102);
			response.setResponseMsg("秒杀失败，商品已经售罄");
			response.setRefreshTime(0);
		} finally {
			logger.info("开始释放锁资源...");
			redissonDistributedLocker.unlock(lockKey);  //释放锁
		}
		return response;
	}
	
	/**
	 * 判断秒杀活动是否已经开始
	 * <p>Title: checkStartSeckill</p>  
	 * <p>Description: </p>  
	 * @param stallActivityId
	 * @return
	 */
	@Override
	public boolean checkStartSeckill(int stallActivityId) {
		//此处已经省略了业务代码，良好的操作时应该将秒杀活动的开始时间在新增/编辑主数据的是维护到redis中，并维护好key值，此处取出，然后做出判断
		//默认为开始了
		return true;
	}
	@Transactional
	@Override
	public BaseResponse createOrder(long seckillId, long userId, long number) {
		String nativaSql="SELECT number FROM seckill WHERE seckill_id=?";
		Object object = dynamicQuery.nativeQueryObject(nativaSql, new Object[]{seckillId});
		long value = ((Number) object).longValue();
		if(value>0){
			//nativaSql="UPDATE seckill  SET number=number-1 WHERE seckill_id=?";
			nativaSql="UPDATE seckill  SET number=number-1 WHERE seckill_id=? AND number>0";
			int res = dynamicQuery.nativeExecuteUpdate(nativaSql, new Object[]{seckillId});
			if(res>0){
				SuccessKilled successKilled = new SuccessKilled();
				successKilled.setCreateTime(new Timestamp(new Date().getTime()));
				successKilled.setSeckillId(seckillId);
				successKilled.setUserId(userId);
				successKilled.setState((short)0);
				dynamicQuery.save(successKilled);
			}else{
				//更新没有成功
				return new BaseResponse(false,6406,"商品售罄");
			}
		}else{
				return new BaseResponse(false,6406,"商品售罄");
		}
		return  new BaseResponse();
	}
//		boolean res=false;
//		try {
//			//尝试获取锁，最多等待3秒，上锁以后10秒自动解锁（实际项目中推荐这种，以防出现死锁）
//			res = redissonDistributedLocker.tryLock(seckillId+"", TimeUnit.SECONDS, 3, 10);
//			if(res){
//				String nativeSql = "SELECT number FROM seckill WHERE seckill_id=?";
//				//Seckill seckill = xiadan.getOne(seckillId);
//			//	Object object = xiadan.nativeQueryObject(nativeSql, new Object[]{seckillId});
//			//	long count=((Number) object).longValue();
//				//Object object =  dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
//				//Long count =  ((Number) object).longValue();
//				if(1>=number){
//				//if(count>=number){
//					SuccessKilled killed = new SuccessKilled();
//					killed.setSeckillId(seckillId);
//					killed.setUserId(userId);
//					killed.setState((short)0);
//					killed.setCreateTime(new Timestamp(new Date().getTime()));
//					//xiadan.save(killed);
//					//dynamicQuery.save(killed);
//					nativeSql = "UPDATE seckill  SET number=number-? WHERE seckill_id=? AND number>0";
//					//xiadan.flush();
//					//xiadan.nativeExecuteUpdate(nativeSql,new Object[]{number,seckillId});
//					//dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{number,seckillId});
//				}else{
//					return new BaseResponse(false, 6405, "活动太火爆，已经售罄啦！");
//				}
//			}else{
//				return new BaseResponse(false, 6406, "排队中");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally{
//			if(res){//释放锁
//				redissonDistributedLocker.unlock(seckillId+"");
//			}
//		}
//		return new BaseResponse();
//	}
	}
