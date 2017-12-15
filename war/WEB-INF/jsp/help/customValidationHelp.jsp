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