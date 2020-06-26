package zhaoshuo.seckill.response;
/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-23 13:35
 */
public class StockNumResponse extends BaseResponse {
	
	private Long stockNum;
	
	private Long realStockNum;

	public Long getStockNum() {
		return stockNum;
	}

	public void setStockNum(Long stockNum) {
		this.stockNum = stockNum;
	}

	public Long getRealStockNum() {
		return realStockNum;
	}

	public void setRealStockNum(Long realStockNum) {
		this.realStockNum = realStockNum;
	}

}
