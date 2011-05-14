package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*

public class ValidationExceptionTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ValidationExceptionTest.class);
	}

	//ValidationException(String)

	@Test(expected = ValidationException.class)
	public void ValidationException_stringMessage_exceptionThrownWithMsg() {
		def msg = "the error msg"
		try {
			throw new ValidationException(msg)
		} catch (e) {
			assertEquals(msg, e.getMessage())
			throw e
		}
	}

	//ValidationException(Integer)

	@Test(expected = ValidationException.class)
	public void ValidationException_errorNumberInt_exceptionThrownWithMsg() {
		def num = 1
		def msg = ErrorCodes.getText(num)
		try {
			throw new ValidationException(num)
		} catch (e) {
			assertEquals(msg, e.getMessage())
			throw e
		}
	}

	//ValidationException(BigDecimal)

	@Test(expected = ValidationException.class)
	public void ValidationException_errorNumberBigDecimal_exceptionThrownWithMsg() {
		def num = 2.01
		def msg = ErrorCodes.getText(num)
		try {
			throw new ValidationException(num)
		} catch (e) {
			assertEquals(msg, e.getMessage())
			throw e
		}
	}

	//ValidationException(Integer, String)

	@Test(expected = ValidationException.class)
	public void ValidationException_errorNumWithAddString_exceptionThrownWithMsg() {
		def num = 1
		def msg = ErrorCodes.getText(num)
		def msgadd = " add this to the error text"
		try {
			throw new ValidationException(num, msgadd)
		} catch (e) {
			assertEquals(msg + msgadd, e.getMessage())
			throw e
		}
	}

}