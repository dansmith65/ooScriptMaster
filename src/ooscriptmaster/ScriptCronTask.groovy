package ooscriptmaster

import it.sauronsoftware.cron4j.Task
import it.sauronsoftware.cron4j.TaskExecutionContext

/**
 * Task class that is scheduled by cron4j
 */
// TODO don't allow task to run multiple times in a row if a past task was not run on time
public class ScriptCronTask extends Task {
	public Object fmpro
	public String file
	public String script
	public String parameter
	public Integer priority

	public ScriptCronTask(Object fmpro, String script, String parameter, String file, Integer priority) {
		this.fmpro = fmpro
		this.script = script
		this.parameter = parameter
		this.file = file
		this.priority = priority
	}

	public void execute(TaskExecutionContext context) {
		if (priority != null) {
			Thread.currentThread().setPriority(priority)
		}
//		fmpro.performScript(file, script, parameter)
		fmpro.performScript(file, script, parameter +
				' (priority:' + Thread.currentThread().getPriority().toString()) + ')'
	}
}