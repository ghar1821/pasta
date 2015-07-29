package pasta;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Before;

public class PASTAJUnitTest {

    private static ByteArrayOutputStream out = new ByteArrayOutputStream();

    @BeforeClass
	public static void initClass() {
		System.setOut(new PrintStream(out, true));
		System.setSecurityManager(new PASTASecurityManager());
	}

    @Before
	public void initTest() {
		clearOutput();
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

    protected final void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
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
