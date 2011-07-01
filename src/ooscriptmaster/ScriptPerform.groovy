package ooscriptmaster

/**
 * Perform FileMaker script immediately.
 */
public class ScriptPerform extends Functions {

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Perform FileMaker script immediately.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param script name to run
	 * @param parameter to send to script
	 * @param file name to perform script in (default = current file)
	 * @return true if completed without error
	 */
	public static Boolean perform(fmpro, script, parameter, file) {
		paramRequired(script)

		if(file==null || file==''){
			file = fmpro.evaluate('Get(FileName)')
		}

		try{
			fmpro.performScript(file, script, parameter)
		}catch(e){
			ErrorCodes.setLastError(1001, e)
			throw(e)
		}

		success()
	}
}
