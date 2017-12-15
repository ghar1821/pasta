<%--
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h1>How to Create Unit Tests</h1>

<div class='section'>
	<div class='part'>
		<h2 class='part-title'>Main Test Components</h2>
		<dl>
			<dt>Name<dd><p>This is a label for the unit test and must not be empty.
			<dt>Has been tested<dd><p>All unit tests should be tested with at least one sample submission to make sure they work as expected. 
				<p>Once you have tested your unit test, you will have the option to mark the unit test as "tested".
			<dt>Submission base directory<dd><p>If students will be submitting many files, you can set a base directory for code.
				<p>For example, if your students will have a <code>report</code> directory as well as a <code>code</code> directory, you can set the base directory to <code>code</code>. This will ignore anything in other directories when running the test.
		</dl>
		<p>Any unit test can have a combination of black box tests and custom JUnit tests as described below.
	</div>
</div>

<div class='section'>
	<div class='part'>
		<h2 class='part-title'>Black Box Tests</h2>
		<p>Black box tests are compatible with the following languages: Java, Python, C and C++.
		
		<p>Any black box tests created will be run first (before any custom JUnit Tests), and can be used either to exactly compare user output to given expected output, or to run user code against sets of inputs with the intention of performing more complex tests on the output later.
		
		<p>The following fields can be filled out for each black box test case:
		<dl>
			<dt>Test name (required)<dd><p>This must be a valid Java identifier, and so must not contain any whitespace.
			<dt>Timeout (required)<dd><p>The maximum time (in milliseconds) that this test has to run.
			<dt>Command line arguments<dd><p>Command line arguments as they would be written on the command line.
			<dt>Console input<dd><p>The contents of std in for this test.
			<dt>Compare user output<dd><p>This should be checked if you intend to run this test case as a straight comparison of user output to std out with a given expected output.
				<p>If this is unchecked, the test case will not show up as a test result to the student, but the input given will still be run on the student's program.
			<dt>Console output<dd><p>(Only used if "Compare user output" is checked.) The expected contents of std out after the program is run. This will be matched exactly (line by line).
		</dl>
	</div>
</div>

<div class='section'>
	<div class='part'>
		<h2 class='part-title'>Custom JUnit Test</h2>
		<p>If you want to either a) run more complex tests of submitted Java code; or b) run more complex tests on user output in any language (even non-programming assignments); then you'll need to set up a JUnit test. 
		
		<h3>Writing the test</h3>
		<p>To write a test suite, create a JUnit class that extends <code>PASTAJUnitTest</code>.
		<pre><code><c:out value="${PASTAJUnitTest}" /></code></pre>
		<p>For example, the class below will test a simple Java Hello World program.
		<h3>Important!</h3> 
		<p><strong>It is very important that you include test timeouts to the tests</strong>; e.g. <code>@Test(timeout=1000)</code>, as otherwise students who submit code with infinite loops will cause large delays to other students.
		<p><strong>Note:</strong> you must import <code>pasta.PASTAJUnitTest</code> to extend it. You do not need to submit PASTAJUnitTest as this will be provided for you.
		<pre><code><c:out value="${HelloWorldTest}" /></code></pre>
		<p>As another example, the following class will test to see if the user has created a file (<code>outputFile.txt</code>), and also check to see if the results of a given test case were at least a given length.
		<p><strong>Note:</strong> user output will be found in the relative directory "<code>${userout}</code>". Output to std out from a test case named <code>testCase1</code> will be found in a file of the same name.
		<pre><code><c:out value="${SampleCustomTest}" /></code></pre>
		
		<p>If your test requires multiple classes, simple zip the code into a .zip file and submit that.
		
		<h4>Reading test meta-information</h4>
		<p>When writing your tests, you can read some information about each test, including running time, memory usage and exit code. Unit test meta information for a test called <code>testCase1</code> can be found in the relative directory "<code>${usermeta}/testCase1</code>".
		<p>An example of the contents of a file:
		<pre>real 0.36
user 0.07
sys 0.00
memory 25400
exit 0</pre>
		<p>This means that the test had the following attributes:
		<ul>
			<li>Time spent processing was 0.36 seconds
			<li>Time spent processing in user mode was 0.07 seconds
			<li>Time spent processing in kernel mode was 0.00 seconds
			<li>Maximum resident set size of the process was 25400 kB
			<li>The exit code of the process was 0
		</ul>
		
		<p><strong>Note:</strong> if a black box test times out, the meta information will not be saved to the expected file.
		
		<h3>Choosing a main file</h3>
		<p>Every custom JUnit test requires a main test file. You can choose this file from the drop down box on the unit test details page.
		
		<h3>Designing tests</h3>
		<p>It is generally a good idea to make sure that none of your JUnit assert statements that interact with student code reveal any test logic.
		<p>For example, consider the following test code:
		<pre><code>assertEquals(4, StudentClass.doCalculation(8));</code></pre>
		<p>If the student has submitted code that doesn't include the <code>StudentClass.doCalculation</code> method, then they will be shown a compiler error including that line of code, revealing that you are testing the input value 8.
		<p>A better approach would be to do this:
		<pre><code>int expected = 4;
int input = 8;
int actual = StudentClass.doCalculation(input);
assertEquals(expected, actual);</code></pre>
		<p>Now, if the student doesn't have the required method, then they will be shown only line 3, which does not show any test logic.
	</div>
</div>

<div class='section'>
	<div class='part'>
		<h2 class='part-title'>Testing the Unit Test</h2>
		<p>To test, upload a submission that resembles what a student would submit to the assessment. The output provided will show what a student would see.
		<p>If you are using black box tests, you will be required to input a "Solution Name". This is the expected name of the file to try to run (without an extension).
		<p>Once you are satisfied with the performance of the tests, click "Mark as working and tested" to indicate that the unit test is tested.
	</div>
</div>
