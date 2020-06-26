package zhaoshuo.seckill.Exception;

/**
 * @Description
 * @Author zhaoshuo
 * @Date 2020-04-23 13:35
 */
public class IllegalReentrantException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalReentrantException(Throwable cause) {
        super(cause);
    }

    public IllegalReentrantException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalReentrantException(String message) {
        super(message);
    }

    public IllegalReentrantException() {
        super();
    }
}