import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A simple, single file, unit test that can be submitted as a bare java file as
 * a new test.
 */
public class MyProgramSimpleTest {

  private static ByteArrayOutputStream out = new ByteArrayOutputStream();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    System.setOut(new PrintStream(out, true));
  }

  @Before
  public void setUp() throws Exception {
    out.reset();
  }

  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test that MyProgram's main method echoes all input.
   */
  @Test(timeout = 2000)
  public void testMain() {
    String input = "1\r\n2\r\n3\r\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    String expected = "1\r\n2\r\n3\r\n".trim();

    MyProgram.main(new String[0]);
    String actual = out.toString().trim();

    assertEquals(expected, actual);
  }

  /**
   * Test that MyProgram's echo method echoes input Characters.
   */
  @Test(timeout = 2000)
  public void testEcho() {
    Character input = '5';
    Character expected = '5';
    Character actual = MyProgram.echo(input);
    assertEquals(expected, actual);
  }

  /**
   * Test that MyProgram's echoFirst method echoes the first Character in an
   * input String.
   */
  @Test(timeout = 2000)
  public void testEchoFirst() {
    String input = "5";
    Character expected = '5';
    Character actual = MyProgram.echoFirst(input);
    assertEquals(expected, actual);
  }

}
