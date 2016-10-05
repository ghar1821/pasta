<!-- 
Copyright (c) 2014, Alex Radu
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


<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<p> Welcome to Programming Assignment Submission and Testing Application (PASTA).

<p> PASTA is a submission system aimed at helping students maximize their marks 
and programming understanding through instant feedback on their code.

<p> Its main purpose is to reduce the marking load on tutors. It has been written for tutors
and lecturers by tutors and lecturers.

<h1 style='text-align:center'>Login to PASTA</h1>
<br />
<form:errors path="loginForm.*">
	<div class="susk-info-bar error"><span class="image"></span>
		<p class="message"><spring:message code="errors.message" /></p>
	</div>
</form:errors>
<form:form method="post" commandName="LOGINFORM" autocomplete="off">
	<div class='part' style="margin:0 auto; display:table;">
		<div class='info-panel'>
			<div class='ip-item'>
				<div class='ip-label'>
					<form:label for="unikey" path="unikey" cssClass="required">UniKey <span class="star-required">*</span></form:label>
				</div>
				<div class='ip-desc'>
					<form:input path="unikey" size="50" />
					<form:errors path="unikey" cssClass="susk-form-errors" element="div" />
					<script>document.getElementById('unikey').focus()</script>
				</div>
			</div>
			<div class='ip-item'>
				<div class='ip-label'>
					<form:label path="password" cssClass="required">Password <span class="star-required">*</span></form:label> 
				</div>
				<div class='ip-desc'>
					<form:password path="password" size="50" />
					<form:errors path="password" cssClass="susk-form-errors" element="div" />
				</div>
			</div>
		</div>
		<div class='button-panel' style='text-align:center;'>
			<button type="submit" id="Submit" name="Submit">Login</button>
		</div>
	</div>
</form:form>