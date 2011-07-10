package ooscriptmaster

import org.json.simple.JSONValue
import org.json.simple.parser.ParseException

/**
 * Create/Parse JSON text.
 */
public class JSON extends Functions {
	public static def parse = null

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Parse and save JSON text, so it can be accessed via other parse... functions.
	 *
	 * @param json text to be parsed
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean parse(json) {
		paramRequired(json)
		try {
			this.parse = JSONValue.parseWithException(json as String)
		} catch (ParseException e) {
			ErrorCodes.setLastError(201, e)
			throw (e)
		}
		success()
	}

	/**
	 * Get value (and sub-values) from a specific location in JSON text that was previously
	 * parsed with {@link #parse}.
	 *
	 * Use a combination of dot notation (.person.name) or ()
	 *
	 *
	 *
	 * @param location to return values from. This is evaluated on the previously parsed value with
	 * groovy.util.Eval class, so it can acually be any valid groovy string that could follow a variable.
	 *  <ul>
	 *      <li>{@code .person.name} is equivalent to {@code ['person']['name']}
	 *      <li>{@code .people.size ( )} will return the size of the people array
	 *      <li>{@code .people[0]} will return the first person
	 *      <li>an empty value in this parameter will return the entire parsed JSON
	 * @return specified value
	 */
	public static def parseValue(location) {
		if (location == null) location = ''

		if (this.parse == null) {
			throw new ValidationException(202)
		}

		def result
		try {
			result = Eval.x(this.parse, 'x' + location)
		} catch (e) {
			ErrorCodes.setLastError(203, e)
			throw (e)
		}

		success(result)
	}

}
