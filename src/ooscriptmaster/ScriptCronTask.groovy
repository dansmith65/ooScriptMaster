package ooscriptmaster

import it.sauronsoftware.cron4j.Task
import it.sauronsoftware.cron4j.TaskExecutionContext

/**
 * Task class that is scheduled by cron4j
 */
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
		fmpro.performScript(file, script, parameter)
	}
}