package ooscriptmaster

import org.junit.*
import junit.framework.JUnit4TestAdapter
import static org.junit.Assert.*

public class TimerTest {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TimerTest.class);
	}

	//start

	@Test public void start_null_returnBigDecimal() {
		assertTrue(Timer.run("start") instanceof BigDecimal)
	}

	//split

	@Test public void split_startedFirst_returnBigDecimal() {
		Timer.run("start")
		assertTrue(Timer.run("split") instanceof BigDecimal)
	}

	@Test(expected = ValidationException.class)
	public void split_timerNotStartedFirst_throwException() {
		Timer.start = null
		Timer.run("split")
	}

	//stop

	@Test public void stop_startedFirst_returnBigDecimal() {
		Timer.run("start")
		assertTrue(Timer.run("stop") instanceof BigDecimal)
	}

	@Test(expected = ValidationException.class)
	public void stop_timerNotStartedFirst_throwException() {
		Timer.start = null
		Timer.run("stop")
	}

	@Test public void stop_timerNotStartedFirst_lastErrorIsCorrect() {
		Timer.start = null
		try {
			Timer.run("stop")
		} catch (e) {
			assertEquals(200, ErrorCodes.lastErrorNumber)
		}
	}
}