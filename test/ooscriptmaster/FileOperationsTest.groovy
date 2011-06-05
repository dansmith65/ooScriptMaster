package ooscriptmaster

import junit.framework.JUnit4TestAdapter
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import static org.junit.Assert.*

/**
 * Many of the methods in the FileOperations class depend on the current operating system and
 * files structure, because of this, many of these test are disabled by default.  To test one of
 * them you will need to un-comment it/remove @Ignore, posibly set a value that works for you
 * current system, then run the test.
 */
public class FileOperationsTest {
	public static String[] validPaths
	public static String[] invalidPaths = [
			'?',
			'fileC:/oosm_test',
			'file:\\C:\\oosm_test',
	]

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(FileOperationsTest.class);
	}

	@BeforeClass public static void runBeforeClass() {
		// run for one time before all test cases
		if (Functions.ISWIN) {
			//TODO: add more common path formats to this list
			validPaths = [
					// first path is used to test directory methods
					'C:\\oosm_test\\35498732167986\\',
					// second path is used to test file methods
					'C:\\oosm_test\\35498732167986\\test.txt',
					'C:/oosm_test/',
					'C:\\\\oosm_test\\\\',
					'file:/C:/oosm_test',
					'file:/C:/oosm_test with spaces/',
			]
		} else if (Functions.ISMAC) {
			//TODO: enter a list of valid paths for a Mac
			validPaths = [
                    '/Users/RWU/Documents/',
                    '/Users/iCafeM7/Documents/',
                    '/Volumes/MacBookPro/Users/RWU/Documents/SpringFM/Demo.txt'
			]
		} else {
			fail("not on Mac or Windows?  what OS???")
		}
	}

	//returnFile

	@Test public void returnFile_validPaths_fileObjectWithValidPath() {
		validPaths.each {
			def f = FileOperations.returnFile(it)
			assertTrue(it, f instanceof File)
			assertTrue(it, f.getCanonicalFile() instanceof File)
		}
	}

	@Test public void returnFile_invalidPaths_exceptionThrownAndErrorCodeSet() {
		try {
			invalidPaths.each { FileOperations.returnFile('???') }
		} catch (e) {
			assertEquals(2.02, ErrorCodes.lastErrorNumber)
		}
	}

	//DIRECTORY METHODS START HERE ============================================

/*
	//directoryDelete

	@Test(expected = ValidationException)
	public void	directoryDelete_validPathDirDoesNotExist_throwException() {
		// directory might already exist, so run the function twice so it can fail
		// the 2nd time for sure
		FileOperations.directoryDelete(validPaths[0])
		FileOperations.directoryDelete(validPaths[0])
	}

	//directoryCreate

	@Test public void directoryCreate_validPath_true() {
		assertTrue(validPaths[0], FileOperations.directoryCreate(validPaths[0]))
	}

	//directoryOpen

	@Test public void directoryOpen_validPath_true() {
		assertTrue(validPaths[0], FileOperations.directoryOpen(validPaths[0]))
	}

	//directoryDeleteContents

	@Test public void directoryDeleteContents_validPath_true() {
		assertTrue(validPaths[0], FileOperations.directoryDeleteContents(validPaths[0]))
	}

	//directoryDelete

	@Test public void directoryDelete_validPath_true() {
		assertTrue(validPaths[0], FileOperations.directoryDelete(validPaths[0]))
	}
*/

	//directoryList

	@Ignore @Test public void directoryList_validPath_doesNotThrowException() {
		def dir = validPaths[0]
		dir = 'D:\\Dan\\Consulting\\Keystone Database\\Plug-In\\ooScriptMaster'
		//fail(FileOperations.directoryList(dir).toString())  //use to view output of method
		FileOperations.directoryList(dir)
	}

	//directoryDesktop

	// view the output of this test to see if it's correct

	@Ignore @Test public void directoryDesktop_none_path() {
		fail(FileOperations.directoryDesktop())
	}

	//FILE METHODS START HERE =================================================

	//fileExists

	@Ignore @Test public void fileExists_validPath_true() {
		def dir =
		'D:\\Dan\\Consulting\\Keystone Database\\Plug-In\\ooScriptMaster'
		def file =
		'D:\\Dan\\Consulting\\Keystone Database\\Plug-In\\ooScriptMaster\\ooScriptMaster.iml'
		assertTrue(dir, FileOperations.fileExists(dir))
		assertTrue(file, FileOperations.fileExists(file))
	}

	@Ignore @Test public void fileExists_invalidPath_false() {
		def dir =
		'C:\\DoesNotExist'
		def file =
		'C:\\DoesNotExist.ext'
		assertFalse(dir, FileOperations.fileExists(dir))
		assertFalse(file, FileOperations.fileExists(file))
	}

	//fileDelete

	@Ignore @Test public void fileDelete_validPathToFile_true() {
		def dir =
		'C:\\oosm_test\\'
		def file =
		'C:\\oosm_test\\a.txt'
		assertTrue(file, FileOperations.fileDelete(file))
		assertTrue(dir, FileOperations.fileDelete(dir))
	}

	//PATH METHODS START HERE =================================================

	//pathIsFile AND pathIsDirectory  (tested in the same method)

	@Test public void pathIsFileAndpathIsDirectory_pathsToFile() {
		def pathsToFile = [
				'C:/BOOTSECT.BAK',
				'C:/test.ext',
				'C:/dir/test.ext',
				'C:\\dir\\test.',
				'C:/dir/test.',
		]
		pathsToFile.each {
			assertTrue('file: ' + it, FileOperations.pathIsFile(it))
			assertFalse('dir: ' + it, FileOperations.pathIsDirectory(it))
		}
	}

	@Test public void pathIsFileAndpathIsDirectory_pathsToDir() {
		def pathsToDir = [
				'C:/',
				'C:/Windows/',
				'C:/Windows',
				'C:\\Windows',
				'C:/dir/',
				'C:/dir',
		]
		pathsToDir.each {
			assertFalse('file: ' + it, FileOperations.pathIsFile(it))
			assertTrue('dir: ' + it, FileOperations.pathIsDirectory(it))
		}
	}

	//pathAsDirectory

	@Ignore @Test public void pathAsDirectory_pathToFileAndDir_path() {
		def dir = 'C:\\oosm_test\\'
		def file = 'C:\\oosm_test\\a.txt'
		file = 'file:/D:/Dan/Consulting/Keystone Database/Plug-In/ooScriptMaster/Documentation.fp7'
		fail(FileOperations.pathAsDirectory(file))
	}

	//pathAsFile

	@Ignore @Test public void pathAsFile_pathToFileAndDir_file() {
		def dir = 'C:\\oosm_test\\'
		def file = 'C:\\oosm_test\\a.txt'
		file = 'file:/D:/Dan/Consulting/Keystone Database/Plug-In/ooScriptMaster/Documentation.fp7'
		fail(FileOperations.pathAsFile(file))
	}

	//pathAsURI

	@Ignore @Test public void pathAsURI_pathToFileAndDir_file() {
		def dir = 'C:\\oosm_test\\'
		def file = 'C:\\oosm_test\\a.txt'
		file = 'file:/D:/Dan/Consulting/Keystone Database/Plug-In/ooScriptMaster/Documentation.fp7'
		fail(FileOperations.pathAsURI(dir))
	}
	//TODO: test other FileMaker URI paths with this function

}