package telegraph.articles.implementation;

import java.util.concurrent.TimeoutException;

public class ServiceTimeoutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3011713453982629956L;

	public ServiceTimeoutException(TimeoutException e) {
		super(e);
	}

}
