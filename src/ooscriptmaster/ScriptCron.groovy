package ooscriptmaster

import it.sauronsoftware.cron4j.Predictor
import it.sauronsoftware.cron4j.Scheduler
import it.sauronsoftware.cron4j.SchedulingPattern
import java.text.DateFormat

/**
 * UNIX cron-like script scheduler.
 */
public class ScriptCron extends Functions {
	/**
	 * cron4j object.
	 */
	public static Scheduler cron = new Scheduler()

	/**
	 * Holds map of Name:ScriptCronTask objects.
	 */
	public static Map tasks = [:]

	/**
	 * Thread priority for cron, valid range is likely from 1-10, but the current environment is
	 * tested to determine the actual valid range. Default value is 9. Other threads in this
	 * plug-in run at level 6. A lower level can cause a delay in the execution of the
	 * script. In my tests on Windows Vista, a level of 6 caused up to 10 min. delay,
	 * and level 9 had very little/no delay.
	 */
	public static Integer priority = 9


	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Throw exception if pattern is invalid.
	 *
	 * @param pattern for a schedule
	 */
	public static void validateSchedule(pattern) {
		try {
			new SchedulingPattern(pattern)
		} catch (e) {
			ErrorCodes.setLastError(2, e,
					': schedule: ' + pattern
			)
			throw (e)
		}
	}

	/**
	 * Throw exception if value cannot be converted to an Integer, or is not in the valid range.
	 * The valid range is determined by the current threads valid priority range.
	 *
	 * @param value to validate
	 * @param allowNull boolean. If true, will not throw Exception if value is null, default = false
	 * @return value as Integer
	 */
	public static Integer validatePriority(value, Boolean allowNull = false) {
		// convert to integer
		try {
			value = value as Integer
		} catch (e) {
			ErrorCodes.setLastError(2.04, e, ' (priority=' + value + ')')
			throw (e)
		}

		// test if null
		if (value == null && allowNull) {return value}

		// validate range
		def validRange = (Thread.currentThread().MIN_PRIORITY..Thread.currentThread().MAX_PRIORITY)
		if (!validRange.contains(value)) {
			throw new ValidationException(2,
					': priority must be in the range of ' +
							validRange.from + '-' + validRange.to
			)
		}
		return value
	}

	/**
	 * Test if a task name exists in both the tasks property and in cron scheduler. If task
	 * exists in tasks property, but not cron scheduler, throw Exception. Optionally throw
	 * Exception if task does not exist in tasks property.
	 *
	 * @param name of task
	 * @param required boolean. If true will throw Exception if task does not exist (default is
	 * false).
	 * @return true if task exists, otherwise false
	 */
	public static Boolean taskExists(String name, Boolean required = false) {
		if (tasks[name] == null) {
			if (required) {
				throw new ValidationException(2,
						': task name does not exist'
				)
			}
			return success(false) as Boolean
		}
		if (cron.getTask(tasks[name] as String) == null) {
			throw new ValidationException(4,
					": task existed in task property, but not in cron"
			)
		}
		return success()
	}

	/**
	 * Add/modify a task in the cron scheduler.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param script name to run
	 * @param parameter to send to script
	 * @param schedule (UNIX crontab-like pattern)
	 *      <a href="http://www.sauronsoftware.it/projects/cron4j/manual.php#p02">cron4j manual</a>
	 * @param options configure how this method will operate
	 * <ul>
	 *      <li><code>file</code> = file name to perform script in (default = current file)
	 *      <li><code>name</code> = name of schedule. If the name already exists, it will be
	 *          replaced. (default = fileName::script)
	 *      <li><code>priority</code> = thread priority for this task. same as {@link #priority},
	 *          except it only applies to this task. (default = current value of {@link #priority})
	 *      <li><code>autostart</code> = boolean. If true, will automatically start the cron
	 *          scheduler after adding this task (default = true)
	 * </ul>
	 * @return name of task created
	 */
	public static String add(fmpro, script, parameter, schedule, options) {
		Map validOptions = [:]
		validOptions.file = fmpro.evaluate('Get(FileName)')
		validOptions.name = validOptions.file + '::' + script
		// passing a null value to the task for priority will cause it to use the currently
		// defined priority for it's parent thread; which should be the value of this.priority
		validOptions.priority = null
		validOptions.autostart = true
		options = parseOptions(options, validOptions)

		paramRequired(script)
		paramRequired(schedule)

		validateSchedule(schedule)
		options.priority = validatePriority(options.priority, true)

		// if task has previously been scheduled, delete it, so it can be scheduled again
		if (taskExists(options.name)) {
			delete(options.name)
		}

		// create task object
		def task = new ScriptCronTask(fmpro, script, parameter, options.file, options.priority)

		// add task to cron; save return value
		String taskId = cron.schedule(schedule, task)

		// add task to tasks property
		tasks[options.name] = taskId

		if (options.autostart) start()

		return success(options.name)
	}

	//TODO add edit method

	/**
	 * Set the priority property and the cron priority if cron is started.
	 *
	 * @param priority
	 * @return
	 */
	public static Boolean prioritySet(value) {
		priority = validatePriority(value)
		if (cron.isStarted()) {
			cron.timer.setPriority(priority)
		}
		success()
	}

	/**
	 * Start cron if not already started, then set it's priority from the value of the priority
	 * property.
	 *
	 * @return true if completed without error
	 */
	public static Boolean start() {
		if (!cron.isStarted()) {
			cron.setDaemon(true)
			cron.start()
		}
		cron.timer.setPriority(priority)
		success()
	}

	/**
	 * Stop cron, which prevents all scheduled tasks from running. The tasks are not
	 * deleted, only paused. If a task is defined to run while cron is stopped,
	 * that task will be skipped; it will NOT run when cron is started (not until it's next
	 * scheduled run time, that is).
	 *
	 * @return true if completed without error
	 */
	public static Boolean stop() {
		if (cron.isStarted()) {
			cron.stop()
		}
		success()
	}

	/**
	 * @return list of all scheduled tasks
	 */
	public static String list() {
		def ret = []
		// create header
		ret << ['task', 'file', 'script', 'parameter', 'priority', 'schedule'].join('|')
		tasks.each {k, v ->
			v = v as String
			k = k as String
			taskExists(k)
			def taskObject = cron.getTask(v)
			def task = []
			task << k
			task << taskObject.file
			task << taskObject.script
			task << taskObject.parameter
			if (taskObject.priority == null) {
				task << priority
			} else {
				task << taskObject.priority
			}
			task << cron.getSchedulingPattern(v)
			ret << task.join('|')
		}
		success(ret.join(EOL))
	}

	/**
	 * Delete a scheduled tasks by name. If no more schedules exist, stop cron.
	 *
	 * @return true if completed without error
	 */
	public static Boolean delete(name) {
		taskExists(name, true)
		cron.deschedule(tasks[name])
		tasks.remove(name)
		if (tasks.size() == 0) {
			stop()
		}
		success()
	}

	/**
	 * Delete all scheduled tasks, then stop cron.
	 *
	 * @return true if completed without error
	 */
	public static Boolean deleteAll() {
		// deletedTasks and try/finally block is used to ensure each task that is de-scheduled is
		// deleted from the tasks list, even if an error occurs while de-scheduling tasks
		def deletedTasks = []
		try {
			tasks.each {k, v ->
				cron.deschedule(v)
				deletedTasks << k
			}
		} finally {
			deletedTasks.each { tasks.remove(it) }
		}
		stop()
		success()
	}

	/**
	 * Determine the trigger times for a schedule pattern.
	 *
	 * @param schedule (UNIX crontab-like pattern)
	 *      <a href="http://www.sauronsoftware.it/projects/cron4j/manual.php#p02">cron4j manual</a>
	 * @param qty of values to return
	 * @return list of TimeStamps
	 */
	// TODO add options to specify a starting date
	// TODO remove seconds from the date format: a schedule will never be
	// executed at any second other than :00
	public static String testSchedule(schedule, qty) {
		validateSchedule(schedule)
		Predictor p = new Predictor(schedule as String)
		qty = qty as Integer
		def ret = []
		for (int i = 0; i < qty; i++) {
			ret << DateFormat.getDateTimeInstance().format(p.nextMatchingDate())
		}
		success(ret.join(EOL))
	}

	/**
	 * Determine the trigger times for all current schedules.
	 *
	 * @param name of task to test
	 * @param qty of values to return
	 * @return list of TimeStamps
	 */
	// TODO add option to speicy starting date
	public static String next(qty) {
		def ret = []
		tasks.each {k, v ->
			String pattern = cron.getSchedulingPattern(v as String)
			ret << k + '\t' + pattern
			ret << testSchedule(pattern, qty) + EOL
		}
		success(ret.join(EOL))
	}

}