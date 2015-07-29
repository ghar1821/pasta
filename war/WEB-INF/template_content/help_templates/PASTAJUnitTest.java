import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class PASTAJUnitTest {
	/**
	 * Get anything that has been sent to std out
	 * since you last cleared the output
	 */
	protected final String getOutput() {/*...*/}

	/**
	 * Get anything that has been sent to std out
	 * since you last cleared the output as an array
	 * of lines.
	 * Split on any line endings (Unix-style or
	 * Windows-style).
	 */
	protected final String[] getOutputLines() {/*...*/}

	/**
	 * Throw away any previous ouput.
	 */
    protected final void clearOutput() {/*...*/}

	/**
	 * Set the contents of std in.
	 * This will discard any unread data.
	 */
    protected final void setInput(String input) {/*...*/}
    
    /**
     * Read the contents of the given file into an array of lines.
     * 
     * @throws FileNotFoundException if the given file is not found.
     */
    protected final String[] readFileLines(File file) 
    		throws FileNotFoundException {/*...*/}
    
    /**
     * Read the contents of the given file into a single String
     * 
     * @throws FileNotFoundException if the given file is not found.
     */
    protected final String readFile(File file) 
    		throws FileNotFoundException {/*...*/}
}
