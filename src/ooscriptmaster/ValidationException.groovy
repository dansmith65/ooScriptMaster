package ooscriptmaster

/**
 * Use for validation errors.
 */
public class ValidationException extends GeneralException {

	public ValidationException(String message) {
		super(message)
	}

	public ValidationException(num) {
		super(num)
	}

	public ValidationException(num, String additionalShortText) {
		super(num, additionalShortText)
	}
}
