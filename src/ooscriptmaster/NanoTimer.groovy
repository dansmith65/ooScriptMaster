package ooscriptmaster

import ooscriptmaster.Functions

/**
 * Created by IntelliJ IDEA.
 * User: John Renfrew
 * Date: 15/07/2011
 * Time: 23:59
 * To change this template use File | Settings | File Templates.
 */
class NanoTimer extends Functions {
    public static def start = null
	public static def split = null

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Start the timer.
	 *
	 * @return an abstract number, representing the current nanotime
	 */
	public static def start() {
		split = null
		start = System.nanotime()
		success(start)
	}

	/**
	 * Measure the time in seconds since split was last called (or since start was called,
	 * if split has not been called yet).
	 *
	 * @return elapsed time in nano fractions of a second
	 */
	public static def split() {
		if (start == null) {
			throw new ValidationException(200)
		}
		// if no split time is saved, use start time
		def lastTime
		if (split == null) {
			lastTime = start
		} else {
			lastTime = split
		}
		split = System.nanotime()
		def returnVal = split - lastTime
        returnVal = returnVal / 1000000000
		success(returnVal)
	}

	/**
	 * Stop the timer.
	 *
	 * @return elapsed time in nano fractions of a second (since start was called)
	 */
	public static def stop() {
		if (start == null) {
			throw new ValidationException(200)
		}
		def returnVal = System.nanotime() - start
        returnVal = returnVal / 1000000000
		start = null
		split = null
		success(returnVal)
	}

}
