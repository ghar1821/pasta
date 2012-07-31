package task1;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import security.CustomSecurityManager;

public class SimpleCalcTest {
	
	private static ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	@BeforeClass
	public static void permissionSetup(){
		System.setOut(new PrintStream(out, true));
		System.setSecurityManager(new CustomSecurityManager());
	}

	@Test(timeout=1000)
	public void SimpleTest() {
		out.reset();
		String input = "1 2 3 4 5 6";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(6, in.nextInt());
		assertEquals(3.5, in.nextDouble(), 0.01);
		assertEquals(1, in.nextInt());
		assertEquals(5, in.nextInt());
	}

	@Test(timeout=1000)
	public void SimpleTest2() {
		out.reset();
		String input = "10 20 30 40 50 60";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(6, Integer.parseInt(in.nextLine()));
		assertEquals(35, Double.parseDouble(in.nextLine()), 0.01);
		assertEquals(10, Integer.parseInt(in.nextLine()));
		assertEquals(50, Integer.parseInt(in.nextLine()));
	}
	
	@Test(timeout=1000)
	public void OneInput() {
		out.reset();
		String input = "1";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(1, in.nextInt());
		assertEquals(1, in.nextDouble(), 0.01);
		assertEquals(1, in.nextInt());
		assertEquals(0, in.nextInt());
	}
	
	@Test(timeout=1000)
	public void AllAboveZero() {
		out.reset();
		String input = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(20, in.nextInt());
		assertEquals(10.5, in.nextDouble(), 0.01);
		assertEquals(1, in.nextInt());
		assertEquals(19, in.nextInt());
	}
	
	@Test(timeout=1000)
	public void AllZeroButOne() {
		out.reset();
		String input = "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(16, in.nextInt());
		assertEquals(0.0625, in.nextDouble(), 0.01);
		assertEquals(0, in.nextInt());
		assertEquals(1, in.nextInt());
	}
	
	@Test(timeout=1000)
	public void AllNegative() {
		out.reset();
		String input = "-1 -2 -3 -4 -5 -6";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(6, in.nextInt());
		assertEquals(-3.5, in.nextDouble(), 0.01);
		assertEquals(-6, in.nextInt());
		assertEquals(5, in.nextInt());
	}
	
	@Test(timeout=1000)
	public void BotheNegativeAndPositive() {
		out.reset();
		String input = "-1 -2 -3 4 5 6";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(6, in.nextInt());
		assertEquals(1.5, in.nextDouble(), 0.01);
		assertEquals(-3, in.nextInt());
		assertEquals(9, in.nextInt());
	}

	@Test(timeout=1000)
	public void OneLargeNumberAndSomeSmallerOnes() {
		out.reset();
		String input = "1 214748 3 4 5 6";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(6, in.nextInt());
		assertEquals(35794.5, in.nextDouble(), 0.01);
		assertEquals(1, in.nextInt());
		assertEquals(214747, in.nextInt());
	}

	@Test(timeout=1000)
	public void OneLargeNumber() {
		out.reset();
		String input = "2147483";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(1, in.nextInt());
		assertEquals(2147483, in.nextDouble(), 0.01);
		assertEquals(2147483, in.nextInt());
		assertEquals(0, in.nextInt());
	}

	@Test(timeout=1000)
	public void LotsOfNumbers() {
		out.reset();
		String input = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 20";
		SimpleCalc.main(input.split(" "));
		Scanner in = new Scanner(out.toString());
		assertEquals(222, in.nextInt());
		assertEquals(1.0855855855855856, in.nextDouble(), 0.01);
		assertEquals(1, in.nextInt());
		assertEquals(19, in.nextInt());
	}
}
