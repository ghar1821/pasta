import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * A more complicated example of a unit test. This has multiple files and a
 * customised build.xml. This would be submitted as a zip containing the io and
 * test directories from the code directory, and the build.xml.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyProgramTest {

  // figure out which language is being used
  private static ByteArrayOutputStream out = new ByteArrayOutputStream();
  private static ByteArrayOutputStream err = new ByteArrayOutputStream();

  private int timeout = 60000;

  private final String solutionName = "MyProgram";
  private final static String[] ignoredFiles = { "MyProgramTest.java",
      "CustomSecurityManager.java" };

  private enum Language {
    JAVA, CPP, PYTHON, MATLAB
  };

  private LinkedList<Process> startedProcesses = new LinkedList<Process>();

  private static Language languageUsed;

  @BeforeClass
  public static void permissionSetup() {
    System.setOut(new PrintStream(out, true));
    languageUsed = findLanguage(new File(".").listFiles());

    // System.setSecurityManager(new security.CustomSecurityManager());
  }

  @After
  public void cleanup() {
    for (Process p : startedProcesses) {
      p.destroy();
    }
    startedProcesses.clear();
  }

  public static Language findLanguage(File... files) {
    for (File f : files) {
      if (f.isFile()) {
        boolean ignore = false;
        for (String s : ignoredFiles) {
          if (f.getAbsolutePath().endsWith(s)) {
            ignore = true;
            break;
          }
        }
        if (!ignore) {
          if (f.getAbsolutePath().endsWith(".java")) {
            return Language.JAVA;
          }
          if (f.getAbsolutePath().endsWith(".py")) {
            return Language.PYTHON;
          }
          if (f.getAbsolutePath().endsWith(".cpp")
              || f.getAbsolutePath().endsWith(".cc")
              || f.getAbsolutePath().endsWith(".c")) {
            return Language.CPP;
          }
          if (f.getAbsolutePath().endsWith(".m")) {
            return Language.MATLAB;
          }
        }
      } else if (f.isDirectory()) {
        Language l = findLanguage(new File(f.getAbsolutePath()).listFiles());
        if (l != null) {
          return l;
        }
      }
    }
    return null;
  }

  @Before
  public void cleanOut() {
    out.reset();
    err.reset();
  }

  public void run(String[] parameters, String input)
      throws IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException,
      ClassNotFoundException, IOException, InterruptedException {
    // if the language is not set print a default message
    if (languageUsed == null) {
      PrintWriter pr = new PrintWriter(out);
      pr.println("Language not recognised, please contact your unit coordinator.");
      pr.close();
      return;
    }

    switch (languageUsed) {
      case JAVA:
        runJava(parameters, input);
        break;
      case CPP:
        runCPP(parameters, input);
        break;
      case PYTHON:
        runPython(parameters, input);
        break;
      case MATLAB:
        runMatlab(parameters, input);
        break;
      default:
        // if the language is not recognized, print default message
        PrintWriter pr = new PrintWriter(out);
        pr.println("Language not recognised, please contact your unit coordinator.");
        pr.close();
        break;
    }
  }

  public void runJava(String[] parameters, String input) throws IOException {
    String command = "/usr/local/bin/pastarun timeout -s9 " + (timeout / 1000)
        + "s java -jar " + solutionName + ".jar";
    for (String s : parameters) {
      command += " " + s;
    }
    Process runner = Runtime.getRuntime().exec(command);
    startedProcesses.add(runner);

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        runner.getInputStream()));

    BufferedReader errorReader = new BufferedReader(new InputStreamReader(
        runner.getErrorStream()));

    if (input != null) {
      OutputStream stdin = runner.getOutputStream();
      stdin.write(input.getBytes());
      stdin.flush();
      stdin.close();
    }
    try {
      PrintWriter compileOut = new PrintWriter(new BufferedWriter(
          new FileWriter("inputstring", true)));
      compileOut.println(input);
      compileOut.close();
    } catch (IOException e) {
    }

    PrintWriter pr = new PrintWriter(out);
    PrintWriter errorPrinter = new PrintWriter(err);
    String line = "";
    while ((line = reader.readLine()) != null) {
      pr.println(line);
    }
    while ((line = errorReader.readLine()) != null) {
      errorPrinter.println(line);
    }
    errorPrinter.close();
    pr.close();
  }

  // Python
  public void runPython(String[] parameters, String input) throws IOException {
    String command = "/usr/local/bin/pastarun timeout -s9 " + (timeout / 1000)
        + "s /usr/local/pypy/bin/pypy " + solutionName + ".py";
    for (String s : parameters) {
      command += " " + s;
    }
    Process runner = Runtime.getRuntime().exec(command);
    startedProcesses.add(runner);

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        runner.getInputStream()));

    BufferedReader errorReader = new BufferedReader(new InputStreamReader(
        runner.getErrorStream()));

    if (input != null) {
      OutputStream stdin = runner.getOutputStream();
      stdin.write(input.getBytes());
      stdin.flush();
      stdin.close();
    }

    PrintWriter pr = new PrintWriter(out);
    PrintWriter errorPrinter = new PrintWriter(err);
    String line = "";
    while ((line = reader.readLine()) != null) {
      pr.println(line);
    }
    while ((line = errorReader.readLine()) != null) {
      errorPrinter.println(line);
    }
    errorPrinter.close();
    pr.close();
  }

  // Matlab
  public void runMatlab(String[] parameters, String input) throws IOException {
    //
    String command = "/usr/local/bin/pastarun matlab -nodisplay -nosplash -nodesktop -nojvm";

    Process runner = Runtime.getRuntime().exec(command);
    startedProcesses.add(runner);

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        runner.getInputStream()));

    BufferedReader errorReader = new BufferedReader(new InputStreamReader(
        runner.getErrorStream()));

    if (input != null) {
      OutputStream stdin = runner.getOutputStream();
      stdin.write(input.getBytes());
      stdin.flush();
      stdin.close();
    }

    PrintWriter write = new PrintWriter(runner.getOutputStream());

    write.print(solutionName + "(");

    boolean first = true;
    for (String s : parameters) {
      if (!first) {
        write.print(",");
      }
      write.print("'" + s + "'");
      first = false;
    }

    write.println(");exit;");

    write.close();

    PrintWriter pr = new PrintWriter(out);
    PrintWriter errorPrinter = new PrintWriter(err);
    String line = "";
    boolean print = false;
    while ((line = reader.readLine()) != null) {
      if (print) {
        pr.println(line.replace(">>", " "));
      }
      if (line.contains("www.mathworks.com.")) {
        print = true;
      }
    }
    while ((line = errorReader.readLine()) != null) {
      errorPrinter.println(line);
    }
    errorPrinter.close();
    pr.close();
  }

  // C++
  public void runCPP(String[] parameters, String input) throws IOException,
      InterruptedException {
    String command = "/usr/local/bin/pastarun timeout -s9 " + (timeout / 1000)
        + "s ./" + solutionName;
    for (String s : parameters) {
      command += " " + s;
    }
    Process runner = Runtime.getRuntime().exec(command);
    startedProcesses.add(runner);

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        runner.getInputStream()));

    BufferedReader errorReader = new BufferedReader(new InputStreamReader(
        runner.getErrorStream()));

    if (input != null) {
      OutputStream stdin = runner.getOutputStream();
      stdin.write(input.getBytes());
      stdin.flush();
      stdin.close();
    }

    runner.waitFor();

    PrintWriter pr = new PrintWriter(out);
    PrintWriter errorPrinter = new PrintWriter(err);
    String line = "";
    while ((line = reader.readLine()) != null) {
      pr.println(line);
    }
    while ((line = errorReader.readLine()) != null) {
      errorPrinter.println(line);
    }
    if (runner.exitValue() == 139) {
      errorPrinter
          .println("Segmentation fault (core dumped), exit code 139 detected");
    }
    errorPrinter.close();
    pr.close();
  }

  // ////////////////////////////////////////////////////////////
  // TESTS //
  // ////////////////////////////////////////////////////////////

  @Test(timeout = 3000)
  public void test1() {
    timeout = 3000;
    String inputFilename = "io/test1.in";
    String outputFilename = "io/test1.out";
    testStdIO(timeout, inputFilename, outputFilename);
  }

  @Test(timeout = 3000)
  public void test2() {
    timeout = 3000;
    String inputFilename = "io/test1.in";
    String outputFilename = "io/test1.out";
    testFileInput(timeout, inputFilename, outputFilename);
  }

  /**
   * Helper methods
   */

  /**
   * Run tests that rely on file input, and stdout.
   * 
   * @param timeout
   * @param inputFilename The name of the input file.
   * @param outputFilename The name of a file containing the output string.
   */
  public void testFileInput(int timeout, String inputFilename,
      String outputFilename) {
    this.timeout = timeout;
    try {
      run(new String[] { inputFilename }, null);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (!err.toString().isEmpty()) {
      if (languageUsed == Language.PYTHON || languageUsed == Language.MATLAB) {
        try {
          PrintWriter compileOut = new PrintWriter(new BufferedWriter(
              new FileWriter("compile.errors", true)));
          compileOut.println(err.toString());
          compileOut.close();
        } catch (IOException e) {
        }
      }
      fail(err.toString());
    }
    try {
      String expected = readFile(outputFilename).trim();
      assertEquals(expected, out.toString().trim());
    } catch (IOException e) {
      fail("Test files could not be read.");
    }
  }

  /**
   * Run tests that rely on stdin and stdout.
   * 
   * @param timeout
   * @param inputFilename The name of a file containing the input string.
   * @param outputFilename The name of a file containing the output string.
   */
  public void testStdIO(int timeout, String inputFilename, String outputFilename) {
    this.timeout = timeout;
    String input = null;
    try {
      input = readFile(inputFilename);
    } catch (IOException e1) {
      fail("Test files could not be read.");
    }
    try {
      run(new String[] {}, input);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (!err.toString().isEmpty()) {
      if (languageUsed == Language.PYTHON || languageUsed == Language.MATLAB) {
        try {
          PrintWriter compileOut = new PrintWriter(new BufferedWriter(
              new FileWriter("compile.errors", true)));
          compileOut.println(err.toString());
          compileOut.close();
        } catch (IOException e) {
        }
      }
      fail(err.toString());
    }
    String expected;
    try {
      expected = readFile(outputFilename).trim();
      assertEquals(expected, out.toString().trim());
    } catch (IOException e) {
      fail("Test files could not be read.");
    }
  }

  private static String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }
}
