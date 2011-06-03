package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*

/**
 * Created by IntelliJ IDEA.
 * User: Dan
 * Date: 17/05/11
 * Time: 7:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScriptCronTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ScriptCronTest.class);
	}
	/*
    @BeforeClass public static void runBeforeClass() {
        // run for one time before all test cases
    }
    @AfterClass public static void runAfterClass() {
        // run for one time after all test cases
    }

    @Before public void setUp() {
    }
    @After public void tearDown() {
    }
    */

	//method

	@Ignore @Test public void method_test_expectedResult() {
		Object fmpro = new Object()
//		ScriptCron.perform(fmpro, 'script', 'param', 'schedule=0 * * * *')
		ScriptCron.stop()
	}

	@Test public void validatePriority_valid_noException() {
		ScriptCron.validatePriority(10)
	}

	@Test(expected = ValidationException.class)
	public void validatePriority_invalidHigh_throwException() {
		ScriptCron.validatePriority(11)
	}

	@Test(expected = ValidationException.class)
	public void validatePriority_invalidLow_throwException() {
		ScriptCron.validatePriority(0)
	}

	@Test(expected = NumberFormatException.class)
	public void validatePriority_invalidString_throwException() {
		ScriptCron.validatePriority('abc')
	}

	@Test(expected = ValidationException.class)
	public void validatePriority_null_throwException() {
		assertNull(ScriptCron.validatePriority(null))
	}

	@Test public void validatePriority_nullAndAllowNull_returnNull() {
		assertNull(ScriptCron.validatePriority(null, true))
	}

}