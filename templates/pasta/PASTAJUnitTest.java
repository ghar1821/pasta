package pasta;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Before;

public class PASTAJUnitTest {

    private static ByteArrayOutputStream out = new ByteArrayOutputStream();
    private static PipedInputStream inputToUser;
    private static PipedOutputStream inputFromInstructor;

    @BeforeClass
	public static void initClass() {
		System.setOut(new PrintStream(out, true));
		System.setSecurityManager(new PASTASecurityManager());
	}

    @Before
	public void initTest() {
		clearOutput();
		clearInput();
	}

	/**
	 * Throw away anything that has come into std out up until this point.
	 */
    protected final void clearOutput() {
        out.reset();
    }

	/**
	 * Get anything that has been sent to std out since you last cleared the
	 * output.
	 */
    protected final String getOutput() {
        return out.toString();
    }

	/**
	 * Get anything that has been sent to std out since you last cleared the
	 * output as an array of lines. Split on any line endings (Unix-style [\n]
	 * or Windows-style [\r\n]).
	 */
    protected final String[] getOutputLines() {
        Scanner lineScanner = new Scanner(out.toString());
        int lineCount = 0;
        while(lineScanner.hasNextLine()) {
            lineScanner.nextLine();
            lineCount++;
        }
        String[] lines = new String[lineCount];
        lineScanner = new Scanner(out.toString());
        for(int i = 0; i < lines.length; i++) {
            lines[i] = lineScanner.nextLine();
        }
        return lines;
    }

	/**
	 * Throw away any unread input.
	 */
    protected final void clearInput() {
    	try {
    		if(inputToUser != null)
    			inputToUser.close();
    		if(inputFromInstructor != null)
    			inputFromInstructor.close();
    	} catch (IOException e) {}
    	inputToUser = new PipedInputStream();
    	try {
    		inputFromInstructor = new PipedOutputStream(inputToUser);
    	} catch (IOException e) {}
    	System.setIn(inputToUser);
    }
    
	/**
	 * Send input to standard in. Successive calls to this method will queue the
	 * input.
	 * 
	 * @throws IllegalStateException
	 *             if you have already sent terminated input, which closes the
	 *             input stream.
	 */
    protected final void sendInput(String input) {
    	if(input != null && inputFromInstructor != null) {
    		try {
    			inputFromInstructor.write(input.getBytes());
    		} catch(IOException e) {
    			throw new IllegalStateException("Input stream already closed.");
    		}
    	}
    }

	/**
	 * Send input to standard in, and close the stream. This indicates that no
	 * more input should be read.
	 * 
	 * @throws IllegalStateException
	 *             if you have already sent terminated input, which closes the
	 *             input stream.
	 */
    protected final void sendTerminatedInput(String input) {
    	if(input != null && inputFromInstructor != null) {
    		try {
    			inputFromInstructor.write(input.getBytes());
    		} catch(IOException e) {
    			throw new IllegalStateException("Input stream already closed.");
    		}
    	}
    	try {
    		inputFromInstructor.close();
    	} catch(IOException e) { }
    }
    
	/**
	 * Read the contents of the given file into an array of lines.
	 * 
	 * @throws FileNotFoundException
	 *             if the given file is not found.
	 */
    protected final String[] readFileLines(File file) throws FileNotFoundException {
    	Scanner scn = new Scanner(new FileInputStream(file));
    	LinkedList<String> lines = new LinkedList<>();
    	while(scn.hasNext()) {
    		lines.add(scn.nextLine());
    	}
    	scn.close();
    	return lines.toArray(new String[lines.size()]);
    }
    
	/**
	 * Read the contents of the given file into a single String
	 * 
	 * @throws FileNotFoundException
	 *             if the given file is not found.
	 */
    protected final String readFile(File file) throws FileNotFoundException {
    	Scanner scn = new Scanner(new FileInputStream(file)).useDelimiter("\\Z");
    	String content = "";
    	if(scn.hasNext()) {
    		content = scn.next();
    	}
    	scn.close();
    	return content;
    }
    
	/**
	 * Get the output file associated with a specified input-output test. This
	 * file will contain any output that was sent to std out during the
	 * execution of the test case.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the File containing any user output. This file will always exist
	 *         if the test case got executed (even if no ouput was produced).
	 */
    protected final File getTestOutputFile(String testName) {
    	return new File("userout/", testName);
    }
    
	/**
	 * Get the meta information file associated with a specified input-output
	 * test. This file will contain information about test execution such as
	 * time taken and exit code.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the File containing meta information. This file will always exist
	 *         if the test case got executed.
	 */
    protected final File getTestMetaFile(String testName) {
    	return new File("usermeta/", testName);
    }
    
	/**
	 * Check if the given input-output test case timeout out in execution.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return true if the execution of the test case exceeded the allowed
	 *         timeout value.
	 */
    protected final boolean testTimedOut(String testName) {
    	return new File("usermeta/", testName + ".timedout").exists();
    }
    
	/**
	 * Check if the given input-output test case resulted in a segmentation
	 * fault.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return true if the execution of the test case resulted in a segmentation
	 *         fault
	 */
    protected final boolean testSegfaulted(String testName) {
    	return new File("usermeta/", testName + ".segfault").exists();
    }
    
    private String getMetaFileLine(String testName, int line) {
    	try {
    		return readFileLines(getTestMetaFile(testName))[line-1];
    	} catch(FileNotFoundException e) {
    		return null;
    	}
    }
    
	/**
	 * Get the test execution time for a given input-output test.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the time, in seconds, spent running the test case
	 */
    protected final Double getTestRealTime(String testName) {
    	String line = getMetaFileLine(testName, 1);
    	return line == null ? null : Double.parseDouble(line.split(" ")[1]);
    }
    
	/**
	 * Get the time spent processing in user mode for a given input-output test.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the time, in seconds, spent processing in user mode
	 */
    protected final Double getTestUserTime(String testName) {
    	String line = getMetaFileLine(testName, 2);
    	return line == null ? null : Double.parseDouble(line.split(" ")[1]);
    }
    
	/**
	 * Get the time spent processing in kernel mode for a given input-output test.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the time, in seconds, spent processing in kernel mode
	 */
    protected final Double getTestSystemTime(String testName) {
    	String line = getMetaFileLine(testName, 3);
    	return line == null ? null : Double.parseDouble(line.split(" ")[1]);
    }
    
	/**
	 * Get the maximum amount of memory used by a given input-output test.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the maximum resident set size of the process in kB.
	 */
    protected final Integer getTestMemoryUsage(String testName) {
    	String line = getMetaFileLine(testName, 4);
    	return line == null ? null : Integer.parseInt(line.split(" ")[1]);
    }
    
    /**
	 * Get the exit code for a given input-output test case.
	 * 
	 * @param testName
	 *            the name of the input-output test case
	 * @return the exit code for the test case (0 is normal)
	 */
    protected final Integer getTestExitCode(String testName) {
    	String line = getMetaFileLine(testName, 5);
    	return line == null ? null : Integer.parseInt(line.split(" ")[1]);
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface TestDescription {
    	String value();
    }
}
