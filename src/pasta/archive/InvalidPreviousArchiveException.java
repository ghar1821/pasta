package pasta.archive;

public class InvalidPreviousArchiveException extends Exception {

	private static final long serialVersionUID = -6045755241632569937L;

	public InvalidPreviousArchiveException() {
	}

	public InvalidPreviousArchiveException(String message) {
		super(message);
	}

	public InvalidPreviousArchiveException(Throwable cause) {
		super(cause);
	}

	public InvalidPreviousArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPreviousArchiveException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
