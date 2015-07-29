<!-- 
Copyright (c) 2015, Joshua Stretton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
-->

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h1>How to Create Unit Tests</h1>

<h2>Main Test Components</h2>
<div class='vertical-block'>
	<p><dl>
		<dt>Name<dd><p>This is a label for the unit test and must not be empty.
		<dt>Has been tested<dd><p>All unit tests should be tested with at least one sample submission to make sure they work as expected. 
			<p>Once you have tested your unit test, you will have the option to mark the unit test as "tested".
		<dt>Submission base directory<dd><p>If students will be submitting many files, you can set a base directory for code.
			<p>For example, if your students will have a <code>report</code> directory as well as a <code>code</code> directory, you can set the base directory to <code>code</code>. This will ignore anything in other directories when running the test.
	</dl>
	<p>Any unit test can have a combination of black box tests and custom JUnit tests as described below.
</div>

<h2>Black Box Tests</h2>
<div class='vertical-block'>
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

<h2>Custom JUnit Test</h2>
<div class='vertical-block'>
	<p>If you want to either a) run more complex tests of submitted Java code; or b) run more complex tests on user output in any language (even non-programming assignments); then you'll need to set up a JUnit test. 
	
	<h3>Writing the test</h3>
	<p>To write a test suite, create a JUnit class that extends <code>PASTAJUnitTest</code>.
	<pre class="javacode"><code><c:out value="${PASTAJUnitTest}" /></code></pre>
	<p>For example, the following class will test a simple Java Hello World program.
	<p><strong>Note:</strong> you must import <code>pasta.PASTAJUnitTest</code> to extend it. You do not need to submit PASTAJUnitTest as this will be provided for you.
	<pre class="javacode"><code><c:out value="${HelloWorldTest}" /></code></pre>
	<p>As another example, the following class will test to see if the user has created a file (<code>outputFile.txt</code>), and also check to see if the results of a given test case were at least a given length.
	<p><strong>Note:</strong> user output will be found in the relative directory "<code>${userout}</code>". Output to std out from a test case named <code>testCase1</code> will be found in a file of the same name.
	<pre class="javacode"><code><c:out value="${SampleCustomTest}" /></code></pre>
	
	<p>If your test requires multiple classes, simple zip the code into a .zip file and submit that.
	
	<h3>Choosing a main file</h3>
	<p>Every custom JUnit test requires a main test file. You can choose this file from the drop down box on the unit test details page.
</div>

<h2>Testing the Unit Test</h2>
<div class='vertical-block'>
	<p>To test, upload a submission that resembles what a student would submit to the assessment. The output provided will show what a student would see.
	<p>If you are using black box tests, you will be required to input a "Solution Name". This is the expected name of the file to try to run (without an extension).
	<p>Once you are satisfied with the performance of the tests, click "Mark as working and tested" to indicate that the unit test is tested.
</div>
