package ooscriptmaster

/**
 * Store/Manage error codes
 */
public class ErrorCodes {
	public static final String EOL = System.getProperty("line.separator")
	public static def lastErrorNumber = 0
	public static String lastErrorShortText = ""
	public static String lastErrorDebug = ""

	/**
	 * Default error codes and the corresponding text description
	 */
	public static Map code = [
			(-1): "Unknown error",
			0: "No error",

			// 1-999 are reserved for ooscriptmaster errors
			// 1-99 general/broad-use
			1: "Canceled by user",
			2: "Invalid parameter",
			2.01: "Invalid parameter: empty",
			2.02: "Invalid parameter: could not be converted to a path",
			2.04: "Invalid parameter: must be an integer",
			2.05: "Invalid parameter: must be a directory",
			2.06: "Invalid parameter: option",
			3: "Operation not supported",
			4: "Invalid state",
			// 100 file/folder operations
			100: "File does not exist",
			101: "Directory does not exist",
			102: "File already exists",
			103: "Directory already exists",
			104: "Cannot write to file",
			105: "Cannot delete file",
			106: "Cannot read file",
			120: "File is OPEN",
			// 200 specific to particular function
			200: "TimerStart must be called first",
			// 300 FileMaker calculation engine related
			300: "Container field is empty",
			300.01: "Could not get data from container (make sure container field name is passed as text)",
			// 400 Password or security related
			400: "Password error",
			401: "Encrypted file: incorrect password",
			402: "No password supplied",

			// 1000-1999 are reserved for ScriptMaster errors
			1000: "SQL Error",
			1000.01: "SQL Error: must call SQLArray before any other SQLArray... functions",
			1000.02: "SQL Error: value not in array",
	]

	/**
	 * Retrieve the text description of an error code.  Throw an exception if it does not exist.
	 *
	 * @param num error code
	 * @return text description of error code
	 */
	public static String getText(num) throws ValidationException {
		if (code[num] == null) {
			def msg = "Error Codes class: text does not exist for error number: " + num
			lastErrorNumber = num
			lastErrorShortText = msg
			throw new ValidationException(msg)
		}
		return code[num]
	}

	/**
	 * Set last error fields to default values
	 */
	public static void clearLastError() {
		lastErrorNumber = 0
		lastErrorShortText = ""
		lastErrorDebug = ""
	}

	/**
	 * Set last error fields to the number/description of the specified error code.
	 *
	 * @param num error code
	 * @return text description of error code
	 */
	public static String setLastError(num) {
		lastErrorNumber = num
		lastErrorShortText = getText(num)
		return lastErrorShortText
	}

	/**
	 * Set last error fields to the number/description of the specified error code,
	 * with user-provided code appended to the default description.
	 *
	 * @param num error code
	 * @param additionalShortText text to add to the default error description
	 * @return text description of error code with additionalShortText
	 *                                  appended to it
	 */
	public static String setLastError(num, String additionalShortText) {
		setLastError(num)
		lastErrorShortText += additionalShortText
		return lastErrorShortText
	}

	/**
	 * Set last error fields to the number/description of the specified error code,
	 * add info to lastErrorDebug from the provided Throwable (exception).
	 *
	 * @param num error code
	 * @param cause exception
	 * @return text description of error code
	 */
	public static String setLastError(num, Throwable cause) {
		setLastError(num)
		ErrorCodes.lastErrorDebug +=
			"message: " + cause.getMessage() + EOL +
					cause.getClass() + EOL +
					"cause: " + cause.getCause() + EOL +
					EOL + "========== STACK TRACE ==========" + EOL +
					cause.getStackTrace()
		return lastErrorShortText
	}

	/**
	 * Set last error fields to the number/description of the specified error code,
	 * add info to lastErrorDebug from the provided Throwable (exception).
	 *
	 * @param num error code
	 * @param cause exception
	 * @param additionalShortText text to add to the default error description
	 * @return text description of error code with additionalShortText
	 *                                  appended to it
	 */
	public static String setLastError(num, Throwable cause, String additionalShortText) {
		setLastError(num, cause)
		lastErrorShortText += additionalShortText
		return lastErrorShortText
	}

	/**
	 * Convert a map to CSV formated string.
	 * (this is mostly a helper function for the two 'getAll' methods in this class)
	 *
	 * @param map
	 * @return CSV formatted string (line endings formatted for the current operating system)
	 */
	public static String mapAsCSV(map) {
		map.collect { k, v ->
			[k, v].collect {
				// quote if it contains double-quote or comma
				if (it instanceof String && (it.contains('"') || it.contains(','))) {
					'"' + it.replace('"', '""') + '"'
				} else {
					it
				}
			}.join(',')
		}.join(EOL)
	}

	/**
	 * Return all error codes defined in this class, in CSV format.
	 */
	public static String getAll() {
		return mapAsCSV(code)
	}

	/**
	 * Save all error codes defined in this class, in a CSV formated file.
	 *
	 * @param filePath for output CSV file
	 */
	public static boolean getAllToCSV(String filePath) {
		Functions.paramRequired(filePath)

		// add csv file extension, if it doesn't exist
		if (!filePath.endsWith('.csv')) filePath += '.csv'

		File out = FileOperations.returnFile(filePath)

		// delete output file if it exists
		try {
			if (out.exists()) out.delete()
		} catch (e) {
			setLastError(105, e)
			throw (e)
		}

		// write content to file
		try {
			out.append(mapAsCSV(code))
		} catch (e) {
			setLastError(104, e)
			throw (e)
		}

		return Functions.success()
	}

}
