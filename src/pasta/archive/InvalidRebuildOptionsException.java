package pasta.archive;

public class InvalidRebuildOptionsException extends Exception {
	private static final long serialVersionUID = -858205089563172575L;

	public InvalidRebuildOptionsException() {
	}

	public InvalidRebuildOptionsException(String message) {
		super(message);
	}

	public InvalidRebuildOptionsException(Throwable cause) {
		super(cause);
	}

	public InvalidRebuildOptionsException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRebuildOptionsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
