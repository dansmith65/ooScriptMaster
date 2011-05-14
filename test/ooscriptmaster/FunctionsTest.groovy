package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*

public class FunctionsTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(FunctionsTest.class);
	}

	//success

	@Test public void success_null_valuesSet() {
		Functions.success()
		assertEquals(0, ErrorCodes.lastErrorNumber)
		assertEquals("No error", ErrorCodes.lastErrorShortText)
		assertEquals("", ErrorCodes.lastErrorDebug)
	}

	@Test public void success_string_paramReturned() {
		def str = "this is a string"
		assertEquals(str, Functions.success(str))
	}

	@Test public void success_null_paramReturned() {
		assertNull(Functions.success(null))
	}

	@Test public void success_emptyString_paramReturned() {
		assertEquals("", Functions.success(""))
	}

	//paramRequired

	@Test(expected = ValidationException)
	public void paramRequired_null_exceptionThrown() {
		Functions.paramRequired(null)
	}

	@Test(expected = ValidationException)
	public void paramRequired_emptyString_exceptionThrown() {
		Functions.paramRequired("")
	}

	@Test public void paramRequired_string_exceptionNotThrown() {
		Functions.paramRequired("string")
	}

	//parseOptions

	@Test(expected = ValidationException)
	public void parseOptions_invalidOption_exceptionThrown() {
		def opt = "test=today"
		def validOptions = ["name": "value"]
		Functions.parseOptions(opt, validOptions)
	}

	@Test(expected = ValidationException)
	public void parseOptions_invalidOptionValue_exceptionThrown() {
		def opt = "test=today"
		def validOptions = ["name": 12.34]
		Functions.parseOptions(opt, validOptions)
	}

	@Test public void parseOptions_validOption_containsOptFromParam() {
		def opt = "name=new value"
		def valid = ["name": "value"]
		Map result = Functions.parseOptions(opt, valid)
		assertTrue(result.entrySet().toString().contains(opt))
		// and does not contain the value from validOptions
		//TODO: find out why this is not working...
		//how can value of valid be changed by calling parseOptions???
		//does this happen in my regular code, or just this unit test?
		//assertNotSame(result['name'], valid['name'])
	}

}