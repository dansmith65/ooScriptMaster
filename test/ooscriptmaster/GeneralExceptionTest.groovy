package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*

public class GeneralExceptionTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(GeneralExceptionTest.class);
	}

	//GeneralException(String)

	@Test(expected = GeneralException.class)
	public void GeneralException_stringMessage_exceptionThrownWithMsg() {
		def msg = "the error msg"
		try {
			throw new GeneralException(msg)
		} catch (e) {
			assertEquals(msg, e.getMessage())
			throw e
		}
	}

	//GeneralException(Integer)

	@Test(expected = GeneralException.class)
	public void GeneralException_errorNumberInt_exceptionThrownWithMsg() {
		def num = 1
		def msg = ErrorCodes.getText(num)
		try {
			throw new GeneralException(num)
		} catch (e) {
			assertEquals(msg, e.getMessage())
			throw e
		}
	}

	//GeneralException(BigDecimal)

	@Test(expected = GeneralException.class)
	public void GeneralException_errorNumberBigDecimal_exceptionThrownWithMsg() {
		def num = 2.01
		def msg = ErrorCodes.getText(num)
		try {
			throw new GeneralException(num)
		} catch (e) {
			assertEquals(msg, e.getMessage())
			throw e
		}
	}

	//GeneralException(Integer, String)

	@Test(expected = GeneralException.class)
	public void GeneralException_errorNumWithAddString_exceptionThrownWithMsg() {
		def num = 1
		def msg = ErrorCodes.getText(num)
		def msgadd = " add this to the error text"
		try {
			throw new GeneralException(num, msgadd)
		} catch (e) {
			assertEquals(msg + msgadd, e.getMessage())
			throw e
		}
	}

}