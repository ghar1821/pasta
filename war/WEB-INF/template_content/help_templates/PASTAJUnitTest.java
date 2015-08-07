import java.io.File;
import java.io.FileNotFoundException;

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
     * Throw away any unread input.
     */
    protected final void clearInput() {/*...*/}
    
	/**
	 * Send input to standard in. Successive calls to this
	 * method will queue the input.
	 * 
	 * @throws IllegalStateException if you have already
	 * sent terminated input, which closes the input stream.
	 */
    protected final void sendInput(String input) {/*...*/}
    
    /**
	 * Send input to standard in, and close the stream. 
	 * This indicates that no more input should be read.
	 * 
	 * @throws IllegalStateException if you have already
	 * sent terminated input, which closes the input stream.
	 */
    protected final void sendTerminatedInput(String input) {/*...*/}
    
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
