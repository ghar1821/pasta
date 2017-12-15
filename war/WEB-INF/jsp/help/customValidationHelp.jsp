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

<h1>How to Write Custom Submission Validation</h1>

<div class='section part'>
	<p>If you want to perform custom validation on students' submissions to an assessment before they are accepted, you can submit a custom validator.
	<p>The custom validator is submitted on the update assessment page.
</div>

<div class='section part'>
	<h2 class='part-title'>Writing the Validator</h2>
	<p>You must write a single Java class that implements <code>pasta.service.validation.PASTASubmissionValidator</code>.
	<pre><code><c:out value="${PASTASubmissionValidator}" /></code></pre>
</div>

<div class='section part'>
	<h2 class='part-title'><code>ValidationFeedback</code> Objects</h2>
	<p>Your Validator is going to return collections of <code>pasta.service.validation.ValidationFeedback</code> objects.
	<pre><code><c:out value="${ValidationFeedback}" /></code></pre>
</div>

<div class='section part'>
	<h2 class='part-title'>Example</h2>
	<pre><code><c:out value="${SampleCustomValidator}" /></code></pre>
</div>