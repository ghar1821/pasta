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

<h2>JUnit Test Suite</h2>
<div class='vertical-block'>
	<p>This test type is only compatible with non-programming and Java programming assignments.
	
	<h3>Writing the test</h3>
	<p>To write a test suite, create a JUnit class that extends <code>PASTAJUnitTest</code>.
	<pre class="javacode"><code>${PASTAJUnitTest}</code></pre>
	<p>For example, the following class will test a simple Hello World program.
	<p><strong>Note:</strong> you must import <code>pasta.PASTAJUnitTest</code> to extend it. You do not need to submit PASTAJUnitTest as this will be provided for you.
	<pre class="javacode"><code>${HelloWorldTest}</code></pre>
	<p>If your test requires multiple classes, simple zip the code into a .zip file and submit that.
	
	<h3>Choosing a main file</h3>
	<p>Every JUnit test suite requires a main test file. You can choose this file from the drop down box on the unit test details page.
	
	<h3>Submission base directory</h3>
	<p>If students will be submitting many files, you can set a base directory for code.
	<p>For example, if your students will have a <code>report</code> directory as well as a <code>code</code> directory, you can set the base directory to <code>code</code>. This will ignore anything in other directories when running the test.
	
	<h3>Non-programming tests</h3>
	<p>If you wish to write custom tests, your tests will be executed in the same directory as the student's submission.
	<p>This means that you can access the files of the submission to check for certain files, or check file contents. Simply follow the same procedure as above.
	
	<h3>Testing the unit tests</h3>
	<p>To test, upload a submission that resembles what a student would submit to the assessment. The output provided will show what a student would see.
	<p>Once you are satisfied with the performance of the tests, click "Mark as working and tested" to indicate that the unit test is tested.
</div>

<h2>Black Box Test Suite</h2>
<div class='vertical-block'>
	<p>This test type is compatible with the following languages: Java, Python, C and C++.
	
	<h3>Setting up the tests</h3>
	<p>Black box tests do not require a code submission.
	<p>For each test you add, the following properties can be set:
	<dl>
		<dt>Test name (required)<dd><p>This must be a valid Java identifier, and so must not contain any whitespace.
		<dt>Timeout (required)<dd><p>The maximum time (in milliseconds) that this test has to run.
		<dt>Command line arguments<dd><p>Command line arguments as they would be written on the command line.
		<dt>Console input<dd><p>The contents of std in for this test.
		<dt>Console output<dd><p>The expected contents of std out after the program is run. This must be matched exactly.
	</dl>
	
	<h3>Testing the unit tests</h3>
	<p>If an assessment contains black box unit tests, it must be given a solution name, which is the expected name of the file to try to run. This must also be set when testing the unit test.
	<p>To test, upload a submission that resembles what a student would submit to the assessment. The output provided will show what a student would see.
	<p>Once you are satisfied with the performance of the tests, click "Mark as working and tested" to indicate that the unit test is tested.
</div>
