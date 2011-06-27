package ooscriptmaster

/**
 * Base class for all classes which provide ScriptMaster functions
 */
public class Functions {
	/**
	 * End of line marker for current operating system.
	 */

	public static final String EOL = System.getProperty("line.separator")

	/**
	 * File separator for current operating system
	 */
	public static final String SEP = System.getProperty("file.separator")

	/**
	 * True if the current operating system is Macintosh
	 */
	public static final Boolean ISMAC =
	System.getProperty("os.name").toLowerCase().contains("mac")

	/**
	 * True if the current operating system is Windows
	 */
	public static final Boolean ISWIN =
	System.getProperty("os.name").toLowerCase().contains("windows")

	/**
	 * True if the current Java version is above 1.6
	 */
	public static final Boolean JVER =
	System.getProperty("java.version").substring(0, 2) == '1.' && System.getProperty("java.version").substring(3, 1).toInteger() > 5

	/**
	 * Used to call a method from ScriptMaster. If a method is called directly,
	 * the lastError info will not be set if an Exception is not caught by the method.
	 *
	 * @param instance an instance of the object to call the method in
	 * @param methodName name of method to run
	 * @param args arguments for method
	 * @return result of method that was run
	 */
	public static def run(Object instance, String methodName, Object[] args = null) {
		ErrorCodes.clearLastError()
		try {
			instance.invokeMethod(methodName, args)
		} catch (GeneralException ge) {
			throw ge
		} catch (e) {
			if (ErrorCodes.lastErrorNumber == 0) {
				ErrorCodes.setLastError(-1, e)
			}
			throw (e)
		}
	}

	/**
	 * Set last error info to "No Error".
	 *
	 * @return Boolean true
	 */
	public static Boolean success() {
		success(true) as Boolean
	}

	/**
	 * see success()
	 *
	 * @param returnVal value to return
	 * @return returnVal param
	 */
	public static def success(returnVal) {
		ErrorCodes.lastErrorNumber = 0
		ErrorCodes.lastErrorShortText = ErrorCodes.getText(0)
		ErrorCodes.lastErrorDebug = ""
		return returnVal
	}

	/**
	 * Throw exception if parameter is null or empty.
	 *
	 * @param param parameter to validate
	 */
	public static void paramRequired(param) throws ValidationException {
		if (param == null || param == "") {
			throw new ValidationException(2.01)
		}
	}

	/**
	 * Similar to the Substitute() function in FileMaker: perform multiple replacements in one
	 * function.
	 *
	 * @param value to replace text in
	 * @param replacements : a Map of pairs to replace:  ['\\':'/', '//':'/']
	 * @return value with specified replacements performed
	 */
	public static String substitute(String value, Map replacements) {
		replacements.each { rep, with ->
			value = value.replace(rep, with)
		}
		return value
	}

	/**
	 * Convert user-provided string into a validated Map.
	 *
	 * @param opt list of key=value pairs
	 * @param validOptions map of key's that are allowed, and values to be used as default if not
	 * contained in options parameter
	 * @return map containing all the validOptions, with values overridden by opt
	 */
	public static Map parseOptions(String opt, Map validOptions) {
		Map returnVal = validOptions

		// both parameters must not be empty
		if (opt == null || opt == '') return returnVal
		if (validOptions == null || validOptions == '') return returnVal

		// convert return-delimeted string to map
		Map options = [:]
		opt.splitEachLine('=') { k, v -> options[k] = v }

		// iterate over each option
		// verify it is a valid option
		// convert it to same object type as that from validOption - or throw an error trying
		options.each() { key, value ->
			key = key.toString().toLowerCase()
			if (validOptions.containsKey(key)) {
				// replace default option with user-provided option
				// attempt to convert type based on class of value in validOptions
				try {
					if (validOptions[key] in Boolean) {
						returnVal[key] = value.toString().toBoolean()
					} else if (validOptions[key] == null) {
						// don't perform any conversion of validOption's value is null
						returnVal[key] = value
					} else {
						returnVal[key] = value.asType(validOptions[key].getClass())
					}
				} catch (e) {
					ErrorCodes.setLastError(2.06, e,
							" (value of '${key}' is not valid: ${value})"
					)
					throw (e)
				}
			} else {
				throw new ValidationException(2.06,
						" ('${key}' is not an available option for this function)"
				)
			}
		}
		return success(returnVal) as Map
	}

}