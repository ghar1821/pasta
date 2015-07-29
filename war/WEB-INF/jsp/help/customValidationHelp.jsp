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

<h1>How to Write Custom Submission Validation</h1>
<p>If you want to perform custom validation on students' submissions to an assessment before they are accepted, you can submit a custom validator.
<p>The custom validator is submitted on the update assessment page.

<h2>Writing the Validator</h2>
<p>You must write a single Java class that implements <code>pasta.service.validation.PASTASubmissionValidator</code>.
<pre class="javacode"><code><c:out value="${PASTASubmissionValidator}" /></code></pre>

<h2><code>ValidationFeedback</code> Objects</h2>
<p>Your Validator is going to return collections of <code>pasta.service.validation.ValidationFeedback</code> objects.
<pre class="javacode"><code><c:out value="${ValidationFeedback}" /></code></pre>

<h2>Example</h2>
<pre class="javacode"><code><c:out value="${SampleCustomValidator}" /></code></pre>