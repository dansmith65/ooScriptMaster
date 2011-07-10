package ooscriptmaster

/**
 * Send POST and GET requests to web servers.
 */
public class Web extends Functions {
	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Send GET request to web server.
	 *
	 * @param url AND get request
	 * @param options currently not used
	 * @return
	 */
	// TODO add options: charset,
	public static def get(url, options){
		paramRequired(url)
		def result = new URL(url).getText()
		success(result)
	}
    
}
