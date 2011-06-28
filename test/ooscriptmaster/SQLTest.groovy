package ooscriptmaster

import junit.framework.JUnit4TestAdapter
import org.junit.Test
import static org.junit.Assert.assertEquals

public class SQLTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(SQLTest.class);
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

	//sqlField
	@Test(expected = ValidationException.class)
	public void sqlField_empty_ValidationException() {
		String field = ''
		SQL.sqlField(field)
	}

	@Test(expected = ValidationException.class)
	public void sqlField_invalid_ValidationException() {
		String field = 'a::b::c'
		SQL.sqlField(field)
	}

	@Test public void sqlField_fieldNameOnly_matchString() {
		String field = 'fieldName'
		String expected = '"fieldName"'
		def result = SQL.sqlField(field)
		assertEquals(expected, result)
	}

	@Test public void sqlField_tableAndFieldName_matchString() {
		String field = 'table::field Name'
		String expected = '"table"."field Name"'
		def result = SQL.sqlField(field)
		assertEquals(expected, result)
	}

}