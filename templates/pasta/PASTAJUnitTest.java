package pasta;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
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

    protected final void clearOutput() {
        out.reset();
    }

    protected final String getOutput() {
        return out.toString();
    }

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
    
    protected final void sendInput(String input) {
    	if(input != null && inputFromInstructor != null) {
    		try {
    			inputFromInstructor.write(input.getBytes());
    		} catch(IOException e) {
    			throw new IllegalStateException("Input stream already closed.");
    		}
    	}
    }
    
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
    
    protected final String[] readFileLines(File file) throws FileNotFoundException {
    	Scanner scn = new Scanner(file);
    	LinkedList<String> lines = new LinkedList<>();
    	while(scn.hasNextLine()) {
    		lines.add(scn.nextLine());
    	}
    	scn.close();
    	return lines.toArray(new String[lines.size()]);
    }
    
    protected final String readFile(File file) throws FileNotFoundException {
    	Scanner scn = new Scanner(file).useDelimiter("\\Z");
    	String content = scn.next();
    	scn.close();
    	return content;
    }
}
