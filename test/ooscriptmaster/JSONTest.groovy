package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*
import org.json.simple.parser.ParseException

public class JSONTest {
	public static String json = '{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}'
	public static String jsonBad = '{"person":{"name":"Guillaume"age":33,"pets":["dog","cat"]}}'

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(JSONTest.class);
	}

	//parse

	@Test(expected = ValidationException.class)
	public void method_parse_throwValidationExpecption() {
		JSON.parse('')
	}

	@Test(expected = ParseException.class)
	public void method_parse_throwParseExpecption() {
		JSON.parse(jsonBad)
	}

	@Test public void method_parse_True() {
		assertTrue(JSON.parse(json))
	}

	//parseValue

	@Test public void method_parseValue_matchValue() {
		JSON.parse(json)
		assertEquals("Guillaume", JSON.parseValue(".person.name"))
		assertEquals(33, JSON.parseValue(".person['age']"))
		assertEquals("dog", JSON.parseValue(".person.pets[0]"))
		assertEquals("cat", JSON.parseValue(".person.get('pets')[1]"))
	}

}