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
    	Scanner scn = new Scanner(new FileInputStream(file));
    	LinkedList<String> lines = new LinkedList<>();
    	while(scn.hasNext()) {
    		lines.add(scn.nextLine());
    	}
    	scn.close();
    	return lines.toArray(new String[lines.size()]);
    }
    
    protected final String readFile(File file) throws FileNotFoundException {
    	Scanner scn = new Scanner(new FileInputStream(file)).useDelimiter("\\Z");
    	String content = "";
    	if(scn.hasNext()) {
    		content = scn.next();
    	}
    	scn.close();
    	return content;
    }
    
    protected final File getTestOutputFile(String testName) {
    	return new File("userout/", testName);
    }
    
    protected final File getTestMetaFile(String testName) {
    	return new File("usermeta/", testName);
    }
    
    protected final boolean testTimedOut(String testName) {
    	return new File("usermeta/", testName + ".timedout").exists();
    }
    
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
    
    protected final Double getTestRealTime(String testName) {
    	String line = getMetaFileLine(testName, 1);
    	return line == null ? null : Double.parseDouble(line.split(" ")[1]);
    }
    
    protected final Double getTestUserTime(String testName) {
    	String line = getMetaFileLine(testName, 2);
    	return line == null ? null : Double.parseDouble(line.split(" ")[1]);
    }
    
    protected final Double getTestSystemTime(String testName) {
    	String line = getMetaFileLine(testName, 3);
    	return line == null ? null : Double.parseDouble(line.split(" ")[1]);
    }
    
    protected final Integer getTestMemoryUsage(String testName) {
    	String line = getMetaFileLine(testName, 4);
    	return line == null ? null : Integer.parseInt(line.split(" ")[1]);
    }
    
    protected final Integer getTestExitCode(String testName) {
    	String line = getMetaFileLine(testName, 5);
    	return line == null ? null : Integer.parseInt(line.split(" ")[1]);
    }
}
