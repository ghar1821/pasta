package task4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import security.CustomSecurityManager;

public class SplitterTest {
	
	private static ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	@BeforeClass
	public static void permissionSetup(){
		// TODO
		System.setOut(new PrintStream(out, true));
		//System.setSecurityManager(new CustomSecurityManager());
	}
	
	@Test(timeout=1000)
	public void isInteger() {
		assertTrue(Splitter.isInteger("1"));
	}
	
	@Test(timeout=1000)
	public void isDouble() {
		assertFalse(Splitter.isInteger("1.1"));
	}
	
	@Test(timeout=1000)
	public void isWord() {
		assertFalse(Splitter.isInteger("hello"));
	}
	
	@Test(timeout=1000)
	public void isCharacters() {
		assertFalse(Splitter.isInteger("!@#$%^&*()~"));
	}
	
	@Test(timeout=1000)
	public void isMixed1() {
		assertFalse(Splitter.isInteger("hello123&^%@!#"));
	}
	
	@Test(timeout=1000)
	public void isMixed2() {
		assertFalse(Splitter.isInteger("1potato"));
	}

	@Test(timeout=1000)
	public void isMixed3() {
		assertFalse(Splitter.isInteger("potato3"));
	}
	
	@Test(timeout=1000)
	public void noNumbers1() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input1.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input1-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input1-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("", numbersData);
		assertEquals("Hello World", wordsData);
		assertEquals("0", out.toString().trim());
	}
	
	@Test(timeout=1000)
	public void noNumbers2() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input6.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input6-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input6-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("\r\n\r\n\r\n\r\n\r\n\r\n", numbersData);
		assertEquals("Hello\r\nWorld\r\n!\r\nHow\r\nare\r\nyou?\r\n:)", wordsData);
		assertEquals("0", out.toString().trim());
	}
	
	@Test(timeout=1000)
	public void onlyNumbers1() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input2.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input2-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input2-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("1 2 3", numbersData);
		assertEquals("", wordsData);
		assertEquals("6", out.toString().trim());
	}
	
	@Test(timeout=1000)
	public void onlyNumbers2() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input5.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input5-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input5-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("1\r\n2\r\n3", numbersData);
		assertEquals("\r\n\r\n", wordsData);
		assertEquals("6", out.toString().trim());
	}
	
	@Test(timeout=1000)
	public void bothNumbersAndWords() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input3.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input3-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input3-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("1 2 3", numbersData);
		assertEquals("Hello World", wordsData);
		assertEquals("6", out.toString().trim());
	}
	
	@Test(timeout=1000)
	public void complex1() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input4.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input4-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input4-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("\r\n123451\r\n\r\n10", numbersData);
		assertEquals("Hello World!\r\nthis is robot # reporting for duty.\r\nBeginning scan of sector 10!\r\ncurrent coordinates , 10.4", wordsData);
		assertEquals("123461", out.toString().trim());
	}
	
	@Test(timeout=1000)
	public void complex2() throws FileNotFoundException {
		out.reset();
		Splitter.main("test/input7.txt".split(" "));
		
		Scanner numbersIn = new Scanner(new File("test/input7-numbers.txt"));
		Scanner wordsIn = new Scanner(new File("test/input7-words.txt"));
		
		String numbersData = "";
		String wordsData = "";
		
		while(numbersIn.hasNextLine()){
			numbersData+=numbersIn.nextLine().trim()+"\r\n";
		}
		while(wordsIn.hasNextLine()){
			wordsData+=wordsIn.nextLine().trim()+"\r\n";
		}
		numbersData = numbersData.substring(0, numbersData.length()-2);
		wordsData = wordsData.substring(0, wordsData.length()-2);
		
		assertEquals("\r\n\r\n123451\r\n\r\n\r\n\r\n10", numbersData);
		assertEquals("Hello World!\r\n\r\nthis is robot # reporting for duty.\r\n\r\nBeginning scan of sector 10!\r\n\r\ncurrent coordinates , 10.4", wordsData);
		assertEquals("123461", out.toString().trim());
	}
}
