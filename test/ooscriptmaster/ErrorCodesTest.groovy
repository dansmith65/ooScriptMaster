package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*

public class ErrorCodesTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ErrorCodesTest.class)
	}

	//getText

	@Test public void getText_paramValid_returnStringObject() {
		assertTrue(ErrorCodes.getText(0) instanceof String)
	}

	@Test public void getText_paramValid_returnSpecificString() {
		assertEquals("No error", ErrorCodes.getText(0).toString())
	}

	@Test(expected = ValidationException.class)
	public void getText_paramInvalid_throwException() {
		ErrorCodes.getText(99999999)
	}

	//clearLastError

	@Test public void clearLastError_null_propertiesNull() {
		ErrorCodes.clearLastError()
		assertEquals(0, ErrorCodes.lastErrorNumber)
		assertEquals("", ErrorCodes.lastErrorShortText)
		assertEquals("", ErrorCodes.lastErrorDebug)
	}

}