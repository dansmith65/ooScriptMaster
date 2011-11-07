package ooscriptmaster

/**
 * Measure elapsed time in fractions of a second.
 */
public class Timer extends Functions {
	public static def start = null
	public static def split = null

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Start the timer.
	 *
	 * @return an abstract number, representing the current time
	 */
	public static def start() {
		split = null
		start = System.nanoTime()
		success(start)
	}

	/**
	 * Measure the time in seconds since split was last called
	 * (or since start was called, if split has not been called yet).
	 *
	 * @return elapsed time in fractions of a second
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
		split = System.nanoTime()
		def returnVal = split - lastTime
		success(returnVal / 1000000000)
	}

	/**
	 * Stop the timer.
	 *
	 * @return elapsed time in fractions of a second (since start was called)
	 */
	public static def stop() {
		if (start == null) {
			throw new ValidationException(200)
		}
		def returnVal = System.nanoTime() - start
		start = null
		split = null
		success(returnVal / 1000000000)
	}

}