package ooscriptmaster

/**
 * Base exception for all custom Exceptions thrown by this package.
 */
public class GeneralException extends Exception {

	public GeneralException(String message) {
		super(message)
	}

	public GeneralException(num) {
		super(ErrorCodes.setLastError(num))
	}

	public GeneralException(num, String additionalShortText) {
		super(ErrorCodes.setLastError(num, additionalShortText))
	}

}
