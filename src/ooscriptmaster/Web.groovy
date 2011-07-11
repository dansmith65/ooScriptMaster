package ooscriptmaster

/**
 * Send POST and GET requests to web servers.
 */
// TODO add method for sending POST request
public class Web extends Functions {
	public static def data = []

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Return URL object from string url.
	 *
	 * @param url
	 * @return URL object
	 */
	public static URL returnURL(String url) {
		paramRequired(url)
		URL newURL
		try {
			newURL = new URL(url)
		} catch (e) {
			ErrorCodes.setLastError(2, e, ': error parsing URL')
			throw (e)
		}
		return newURL
	}

	/**
	 * Send GET request to web server. Any data added to this class via setData will be appended
	 * to the provided url.
	 *
	 * @param url
	 * @param options currently not used
	 * @return response from web server as text
	 */
	// TODO add options: charset, get as File (to return data to container field)
	public static def get(url, options) {
		URL urlObj = returnURL(url)

		// append saved data to url query (if any exists)
		if (this.data) {
			// start with query string from url in parameter (if any)
			String query = urlObj.getQuery()
			if (query == null) query = ''

			// add each value from saved data
			this.data.each {
				it.each { k, v ->
					k = URLEncoder.encode(k as String, "UTF-8")
					v = URLEncoder.encode(v as String, "UTF-8")
					query += "&$k=$v"
				}
			}

			// remove leading & from query (it would have been added in the last section if the
			// url in the parameter did not contain a query string)
			if (query.startsWith('&')) query = query.substring(1)

			// create entire url string
			// get url as string
			def urlString = urlObj.toString()
			// remove original query portion, and everything after it
			def removeAfter = urlString.indexOf('?')
			if (removeAfter == -1) removeAfter = urlString.indexOf('#')
			if (removeAfter > -1) urlString = urlString.substring(0, removeAfter)
			// add new query string
			urlString += '?' + query
			// add reference portion of url, if it existed in original url
			if (urlObj.getRef() != null) urlString += '#' + urlObj.getRef()

			// re-create urlObj with new string
			urlObj = new URL(urlString)
		}

		// get url as text
		def result = urlObj.getText()

		success(result)
	}

	/**
	 * Add data that will be sent with the next GET or POST request.
	 *
	 * @param name
	 * @param data
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean setData(name, data) {
		paramRequired(name)
		paramRequired(data)
		this.data << ["$name": data]
		success()
	}

	/**
	 * Clear all data that was added with setData. Use this to reset the values that will be sent
	 * with the next GET or POST request.
	 *
	 * @return true
	 */
	public static Boolean clearData() {
		this.data = []
		success()
	}

	/**
	 * @return data that was set with setData
	 */
	public static def returnData() {
		success(this.data)
	}

}
