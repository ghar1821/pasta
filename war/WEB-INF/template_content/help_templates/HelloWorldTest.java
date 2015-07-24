import static org.junit.Assert.*;
import org.junit.Test;
import pasta.PASTAJUnitTest;

public class HelloWorldTest extends PASTAJUnitTest {
    @Test (timeout = 1000)
    public void doTest() {
        HelloWorld.main(new String[] {});
        String[] output = getOutputLines();
        assertEquals("Hello World!", output[0]);
    }
}
