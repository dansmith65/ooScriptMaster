package ooscriptmaster

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * All methods related to file/directory operations
 */
public class FileOperations extends Functions {

	public static def run(String methodName, Object[] args = null) {
		Functions.run(this, methodName, args)
	}

	/**
	 * Return file object from string path. Can convert a URI into a File.
	 *
	 * @param path to file/directory
	 * @return new File object for path
	 */
	public static File returnFile(String path) {
		paramRequired(path)
		File newFile
		try {
			newFile = new File(path).getCanonicalFile()
			//getCanonicalPath validates the basic format of the path
		} catch (e) {
			// attempt to parse path as a URI (file:/C:/Directory....)
			try {
				URI u = new URI(uriEncode(path))
				newFile = new File(u).getCanonicalFile()
			} catch (e2) {
				ErrorCodes.setLastError(2.02, e2)
				throw (e2)
			}
		}
		return newFile
	}

	/**
	 * Create InputStream object from container field name, validate it, then return it.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param field name
	 * @return InputStream containing container file
	 */
	public static InputStream returnContainerStream(Object fmpro, String field) {
		paramRequired(field)
		InputStream container
		try {
			container = fmpro.getContainerStream(field)
		} catch (e) {
			ErrorCodes.setLastError(300.01, e)
			throw (e)
		}
		if (container == null) {
			throw new ValidationException(300)
		}
		return container
	}

	/**
	 * Encode a string so it can be processed by java.io.File as a URI.
	 * This is necessary because spaces will be replaced with + by default,
	 * which makes the path invalid.
	 *
	 * @param s string to encode
	 * @return encoded string
	 */
	public static String uriEncode(String s) {
		if (s == null || s == '') return s
		String ret
		ret = s.replace(' ', '%20')
		return ret
	}

	/**
	 * Decode a string encoded with uriEncode()
	 *
	 * @param s string to decode
	 * @return decoded string
	 */
	public static String uriDecode(String s) {
		if (s == null || s == '') return s
		String ret
		ret = s.replace(' ', '%20')
		return ret
	}

	//DIRECTORY METHODS START HERE ============================================

	/**
	 * Create a directory, including any necessary but nonexistent parent directories.
	 *
	 * @param path of directory
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean directoryCreate(path) {
		File dir = returnFile(path)
		if (dir.exists()) throw new ValidationException(103)
		if (dir.mkdirs()) return success()

		//Figure out why it failed
		File realParent = dir;
		while (realParent != null && !realParent.exists()) {
			realParent = realParent.getParentFile();
		}
		if (realParent != null && !realParent.canWrite()) {
			ErrorCodes.lastErrorDebug +=
				"parameter: " + path + EOL
			+"converted to path: " + dir.getAbsolutePath() + EOL
			+"parent directory that is not writeable: " + realParent.getAbsolutePath() + EOL
		}
		throw new ValidationException(104)
	}

	/**
	 * Open directory in default file browser.
	 *
	 * @param path of directory
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean directoryOpen(path) {
		File dir = returnFile(path)
		if (!dir.exists()) throw new ValidationException(101)

		// Before Desktop API is used, first check whether the API is supported by this
		// particular virtual machine (VM) on this particular host.
		if (java.awt.Desktop.isDesktopSupported()) {
			java.awt.Desktop dt = java.awt.Desktop.getDesktop()
			dt.open(dir)
			return success()
		} else {
			ErrorCodes.lastErrorDebug += "Desktop.isDesktopSupported() returned false"
			throw new ValidationException(3)
		}
	}

	/**
	 * Delete directory.
	 *
	 * @param path of directory
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean directoryDelete(path) {
		File dir = returnFile(path)
		if (!dir.exists()) throw new ValidationException(101)
		if (!dir.isDirectory()) throw new ValidationException(2.05)
		try {
			dir.deleteDir()
		} catch (e) {
			ErrorCodes.setLastError(105, e)
			throw (e)
		}
		return success()
	}

	/**
	 * Delete all contents of a directory without deleting the directory.
	 *
	 * @param path of directory
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean directoryDeleteContents(path) {
		File dir = returnFile(path)
		if (!dir.exists()) throw new ValidationException(101)
		if (!dir.isDirectory()) throw new ValidationException(2.05)
		try {
			dir.eachDir { it.deleteDir() }
			dir.eachFile { it.delete() }
		} catch (e) {
			ErrorCodes.setLastError(105, e)
			throw (e)
		}
		return success()
	}

	/**
	 * List contents of a directory
	 *
	 * @param path of directory
	 * @param options configure how this method will operate
	 * <ul>
	 *      <li><code>fullpath</code> = boolean. Whether or not to return full paths.
	 *          (default = false)
	 *      <li><code>type</code> = all, file, or directory. Limit the list to a specific type.
	 *          (default = all)
	 *      <li><code>regex</code> = regular expression. If specified, only file/directory
	 *          names that match this expression will be returned. (default = empty)
	 * </ul>
	 * @return true on success, otherwise throw Exception
	 */
	public static String[] directoryList(path, options) {
		Map validOptions = [
				'fullpath': false,
				'type': 'all',
				'regex': '',
		]
		options = parseOptions(options, validOptions)

		paramRequired(path)
		File dir = returnFile(path)
		if (!dir.exists()) throw new ValidationException(101)
		if (!dir.isDirectory()) throw new ValidationException(2.05)

		// validate option.type value
		if (!(options.type in ['all', 'file', 'directory'])) {
			throw new ValidationException(2.06, ': invalid value for type: ' + options.type)
		}

		String[] retVal
		if (options.type == 'all' && !options.regex) {
			// dont use filter if none of the options require it
			if (options.fullpath) {
				retVal = dir.listFiles()
			} else {
				retVal = dir.list()
			}
		} else {
			// compile regex pattern
			Pattern pattern = Pattern.compile(options.regex)
			// create filter
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File directory, String basename) {
					if (options.type == 'file') {
						if (!new File(directory, basename).isFile()) return false
					}
					else if (options.type == 'directory') {
						if (!new File(directory, basename).isDirectory()) return false
					}
					if (options.regex) {
						return basename.matches(pattern)
					}
					return true
				}
			}
			// apply filter to directory
			if (options.fullpath) {
				retVal = dir.listFiles(filter)
			} else {
				retVal = dir.list(filter)
			}
		}

		return success(retVal) as String[]
	}

	/**
	 * @return path to desktop
	 */
	public static String directoryDesktop() {
		// got this code from John Renfrew on FM Forums
		Object fsv = javax.swing.filechooser.FileSystemView.getFileSystemView()

		if (ISMAC) {
			success(fsv.getHomeDirectory().toString() + SEP + 'Desktop' + SEP)
		} else {
			success(fsv.getHomeDirectory().toString() + SEP)
		}
	}

	//FILE METHODS START HERE =================================================

	/**
	 * Determine if a file exists.
	 *
	 * @param path to file
	 * @return true if file exists, false if it doesn't
	 */
	public static Boolean fileExists(path) {
		File file = returnFile(path)
		return success(file.exists()) as Boolean
	}

	/**
	 * Delete a file
	 *
	 * @param path to file to delete
	 * @return true if file deleted, otherwise throw Exception
	 */
	public static Boolean fileDelete(path) {
		File file = returnFile(path)
		if (!file.exists()) throw new ValidationException(100)
		if (file.isDirectory() && file.list().size() != 0) {
			throw new ValidationException(105, ": path is a directory that is not empty")
		}
		try {
			file.delete()
		} catch (e) {
			ErrorCodes.setLastError(105, e)
			throw (e)
		}
		return success()
	}

	//PATH METHODS START HERE =================================================

	/**
	 * Determine if a path points to a file, without requiring the file to exist.
	 *
	 * @param path to file
	 * @return true if path is file, otherwise false
	 */
	public static Boolean pathIsFile(path) {
		File file = returnFile(path)

		// is a File that exists
		if (file.isFile()) return success()

		// is a Directory that exists
		if (file.isDirectory()) return success(false) as Boolean

		// last character in path is a file separator
		if (path.endsWith('\\') || path.endsWith('/')) return success(false) as Boolean

		// last segment of path contains a period
		if (file.getName().contains('.') || path.endsWith('.')) return success()

		return success(false) as Boolean
	}

	/**
	 * Determine if a path points to a directory, without requiring the directory to exist.
	 *
	 * @param path to directory
	 * @return true if path is directory, otherwise false
	 */
	public static Boolean pathIsDirectory(path) {
		File file = returnFile(path)

		// is a File that exists
		if (file.isFile()) return success(false) as Boolean

		// is a Directory that exists
		if (file.isDirectory()) return success()

		// last character in path is a file separator
		if (path.endsWith('\\') || path.endsWith('/')) return success()

		// last segment of path does not contain a period
		if (!file.getName().contains('.') && !path.endsWith('.')) return success()

		return success(false) as Boolean
	}

	/**
	 * Return the path of the directory in the parameter.
	 * (Removes the file name portion of the path)
	 *
	 * @param path to get directory from
	 * @return path to directory, otherwise throw Exception
	 */
	public static String pathAsDirectory(path) {
		File file = returnFile(path)

		if (pathIsDirectory(file.getCanonicalPath())) {
			return success(file.getCanonicalPath() + SEP)
		} else {
			// remove file name from path
			return success(file.getParentFile().getCanonicalPath() + SEP)
		}
	}

	/**
	 * Extract file name portion of a path.
	 *
	 * @param path
	 * @return file name or null if path is a directory
	 */
	public static String pathAsFile(path) {
		File file = returnFile(path)

		if (pathIsDirectory(file.getCanonicalPath())) {
			return success(null)
		} else {
			return success(file.getName())
		}
	}

	/**
	 * Return path as a URI.
	 *
	 * @param path
	 * @return path or null if path is a directory
	 */
	public static String pathAsURI(path) {
		File file = returnFile(path)
		URI u = file.toURI()
		String decoded = URLDecoder.decode(u.toString(), 'UTF-8')
		return success(decoded)
	}

	//ZIP METHODS START HERE ==================================================

	/**
	 * Zip contents of a container field, file, or entire directory.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param src field, file or directory to zip
	 * @param dest path to zip file
	 * @param options configure how this method will operate
	 * <ul>
	 *      <li><code>overwrite</code> = boolean. Whether or not to overwrite output file.
	 *          (default = false)
	 *      <li><code>level</code> = number from 0-9. Compression level for zip file.
	 *          (0 = no compression, 9 = highest compression) (default = 6)
	 *      <li><code>buffer</code> = integer. Buffer size to use when reading/writing to file
	 *          (default = 1024)
	 * </ul>
	 * @return true on success, otherwise throw Exception
	 */
	public static Boolean zip(fmpro, src, dest, options) {
		Map validOptions = [
				'overwrite': false,
				'level': 6,	// level 6 is the default level; same as if it's not specified (at least on my computer)
				'buffer': 1024,
		]
		options = parseOptions(options, validOptions)

		boolean isZipEntryCreated = false
		boolean isZipAlreadyExists = false

		File srcFile
		InputStream srcStream
		File destFile
		BufferedOutputStream destStream

		ZipOutputStream destZip
		String entryName


		try {
			// validate source, create stream from container
			//===============================================================
			paramRequired(src)
			// convert src param to string array with two values (will always contain two values)
			// [0] = path
			// [1] = user-defined location/name in zip file (or null)
			String[] srcArray = src.split("\t")
			String srcPath = srcArray[0]
			String srcRename
			if (srcArray.size() == 1) {
				srcRename = null
			} else if (srcArray.size() == 2) {
				srcRename = srcArray[1]
			} else if (srcArray.size() > 2) {
				throw new ValidationException(2, ": ('source' parameter contained too many tabs)")
			}

			if (srcPath.contains("::")) {
				// FileMaker field
				srcStream = returnContainerStream(fmpro, srcPath)
			} else {
				srcFile = returnFile(srcPath)
				if (!srcFile.exists()) throw new ValidationException(100)
				if (!srcFile.canRead()) throw new ValidationException(106)
			}
			//===============================================================

			// validate destination, create zip stream
			//===============================================================
			destFile = returnFile(dest)
			if (destFile.exists()) {
				if (options.overwrite) {
					destFile.delete()
				} else {
					// TODO: call another method that copies data from zip file to a temp stream,
					// deletes the zip file, then re-creates it be careful not to allow this
					// file to be deleted in the finally block setting isZipEntryCreated to true
					// in this section should work again, for safety,
					// might want to write everything to a temp file,
					// and only delete the destination file after the temp file has been
					// successfully created/closed
					isZipAlreadyExists = true
					ErrorCodes.lastErrorDebug +=
						"zip file already exists, append option is currently not available" + EOL
					throw new ValidationException(102)
				}
			}

			// create destination directory if needed
			if (!destFile.getParentFile().exists()) {
				try {
					directoryCreate(destFile.getParent())
				} catch (e) {
					ErrorCodes.lastErrorDebug +=
						"output directory did not exist, and could not be created" + EOL
					+"parameter value: " + dest + EOL
					throw (e)
				}
			}

			// add .zip file extension, if needed
			if (!destFile.getPath().endsWith('.zip')) {
				destFile = new File(destFile.getCanonicalPath() + '.zip')
			}

			// use a buffered output stream for better performance
			destStream = new BufferedOutputStream(
					new FileOutputStream(destFile),
					options.buffer
			)
			destZip = new ZipOutputStream(destStream)
			destZip.setLevel(options.level)
			//===============================================================

			// zip source
			//===============================================================
			if (srcStream != null) {
				// FILEMAKER FIELD
				entryName = zipEntryName(
						fmpro.getContainerFileName(srcPath),
						srcRename
				)
				zipHelper(srcStream, destZip, entryName, options.buffer)
				isZipEntryCreated = true

			} else if (srcFile.isFile()) {
				// SINGLE FILE
				entryName = zipEntryName(
						srcFile.getName(),
						srcRename
				)
				zipHelper(srcFile, destZip, entryName, options.buffer)
				isZipEntryCreated = true

			} else if (srcFile.isDirectory()) {
				// DIRECTORY

				// save length of base path, so it can be removed from zip entry name
				Integer srcFileLength = srcFile.getCanonicalPath().length()

				srcFile.eachFileRecurse { file ->
					// exclude destination zip file from the input
					// (happens if zip file is placed in the input path)
					if (file.getCanonicalPath() != destFile.getCanonicalPath()) {
						entryName = zipEntryName(
								file.getCanonicalPath().substring(srcFileLength),
								srcRename
						)
						zipHelper(file, destZip, entryName, options.buffer)
						isZipEntryCreated = true
					}
				}

			} else {
				ErrorCodes.lastErrorDebug +=
					"source parameter was not a FileMaker Field, File, or Directory"
				throw new ValidationException(2)
			}
			//===============================================================

		} finally {
			// close streams
			if (srcStream != null) srcStream.close()
			// closing the zip stream when no entries are created throws an exception
			// testing isZipEntryCreated prevents that
			if (isZipEntryCreated && destZip != null) destZip.close()
			if (destStream != null) destStream.close()
			// if there was an Exception, the zip file may have still been created
			// this will delete the newly created, but blank zip file
			if (!isZipEntryCreated && !isZipAlreadyExists && destFile != null)
				destFile.delete()
		}

		return success()
	}

	/**
	 * Add an InputStream or File to a ZipOutputStream with the specified name
	 *
	 * @param input can be any of the following: InputStream, File, or String filePath
	 * @param output ZipOutputStream
	 * @param name name of Zip Entry (file/folder name as it appears in the zip file)
	 * @param buffer size to use when reading from input, defaults to 1024
	 */
	private static void zipHelper(input, ZipOutputStream output, String name,
	                              Integer buffer = 1024) {
		ZipEntry entry

		// convert input parameter to an InputStream
		// adjustments to ZipEntry can be done here (entry.time, for instance)
		InputStream inStream
		try {
			if (input instanceof InputStream) {
				inStream = input
				entry = new ZipEntry(name)
			} else {
				File inFile
				// create File object from parameter
				if (input instanceof File) {
					inFile = input
				} else {
					inFile = returnFile(input)
				}
				// only create stream if it is a file
				if (inFile.isFile()) {
					inStream = new FileInputStream(inFile)
				}

				// define ZipEntry's name
				// use forward slashes, remove double slashes
				if (name != null) name = name.replace('\\', '/').replace('//', '/')
				// make sure directories end with a slash, otherwise it won't be a directory in the zip file
				if (inFile.isDirectory() && !name.endsWith('/')) name += '/'

				entry = new ZipEntry(name)
				entry.time = input.lastModified()
			}

			// add entry to zip file (this prepares for writting data)
			output.putNextEntry(entry)

			// buffered write to stream
			BufferedInputStream inStreamBuffered
			if (inStream != null) {
				try {
					inStreamBuffered = new BufferedInputStream(inStream, buffer)
					output.leftShift(inStreamBuffered)
				} catch (e) {throw (e)}
				finally {
					if (inStreamBuffered != null) inStreamBuffered.close()
				}
			}
		} finally {
			if (inStream != null) inStream.close()
		}
	}

	/**
	 * Determine ZipEntry name based on original file name and user-supplied name/directory
	 * override.
	 *
	 * @param original
	 * @param user
	 * @return
	 */
	private static String zipEntryName(original, user) {
		paramRequired(original)
		// use forward slashes, remove double slashes
		original = original.replace('\\', '/').replace('//', '/')
		// remove leading slash
		if (original.startsWith('/')) original = original.substring(1)

		if (user == null || user == "") return original
		// use forward slashes, remove double slashes
		user = user.replace('\\', '/').replace('//', '/')
		// remove leading slash
		if (user.startsWith('/')) user = user.substring(1)
		if (user == null || user == "") return original

		// user path contains a file extension
		// return user path as-is
		if (user.find('\\.[^\\./]+$') != null) return user

		// user path ends with a period
		// return user path with original file extension
		if (user.endsWith('.')) {
			// the below regex might error if the file does not have an extension(or the path is
			// a folder), and a folder in the path contains a period in it's name
			String standardExt = original.find('[^\\.]+$')
			// if original path does not contain an extension, return original path
			if (standardExt == null) return original
			return user + standardExt
		}

		// now I know that user does not contain a file name and it is not empty
		// therefore, it must be a directory or invalid

		// if user path does not end with a slash, add it so that it's a valid directory
		if (!user.endsWith('/')) user += '/'

		// add user path to front of original path
		return user + original
	}

	//OTHER METHODS START HERE ================================================

	/**
	 * Save FileMaker container field to file.
	 *
	 * @param fmpro object from ScriptMaster
	 * @param containerField name of field in FileMaker
	 * @param path to save containerField to
	 * @param options (currently not used)
	 * @return
	 */
	// TODO: create option to overwrite output file

	public static Boolean exportContainer(fmpro, containerField, path, options) {
		// this will validate the parameter and the container field
		InputStream container = returnContainerStream(fmpro, containerField)

		String containerFileName = fmpro.getContainerFileName(containerField)

		// use forward slashes, remove double slashes
		path = path.replace('\\', '/').replace('//', '/')

		// path does not contain a file extension
		if (path.find('\\.[^\\./]+$') == null) {
			// path ends with a period
			// get file extenstion from container field
			if (path.endsWith('.')) {
				path += containerFileName.find('[^\\.]+$')

				// otherwise, assume that path is a directory
				// and filename should be taken from container file name
			} else {
				// add trailing slash to path, if it does not exist
				if (!path.replace('\\', '/').endsWith('/')) path += '/'
				path += containerFileName
			}
		}

		// validate path
		File output = returnFile(path)

		// create directory(s) if it(they) doesn't exist
		if (!output.getParentFile().exists()) directoryCreate(output.getParent())

		// test if output file exists
		if (output.exists() == true) {
			throw new ValidationException(102)
			// ONLY DELETE FILE IF OPTION OVERWRITE IS TRUE
			//output.delete()
		}

		// create file
		output.append(container)

		// close input stream
		container.close()

		success()
	}
}
