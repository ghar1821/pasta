import static org.junit.Assert.*;
import org.junit.Test;
import pasta.PASTAJUnitTest;
import java.io.File;
import java.io.FileNotFoundException;

public class SampleCustomTest extends PASTAJUnitTest {
    @Test (timeout = 1000)
    public void testFileCreation() {
    	// The student should have created a file in the base 
    	// directory called "outputFile.txt"
    	
        File shouldExist = new File("outputFile.txt");
        if(!shouldExist.exists()) {
        	fail("You didn't create the file " + shouldExist);
        }
    }
    
    @Test (timeout = 1000)
    public void testBlackBoxOutput() {
    	// A Black Box test case called "testCase1" ran the 
    	// code with a certain input. The student should have 
    	// output at least 10 characters to standard out.
    	
    	File outputFile = new File("userout/testCase1");
    	try {
    		String content = readFile(outputFile);
    		assertTrue("You didn't produce enough output!", content.length() >= 10);
    	} catch(FileNotFoundException e) {
    		// This should never happen - the file is created 
    		// even if the user doesn't output anything
    	}
    }
}
