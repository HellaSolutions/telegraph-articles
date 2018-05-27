package telegraph.articles.implementation;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = -1157484263504527891L;

	public ServiceException(String string, Exception e) {
		super(string, e);
	}

	public ServiceException(Exception e) {
		super(e);
	}

}
