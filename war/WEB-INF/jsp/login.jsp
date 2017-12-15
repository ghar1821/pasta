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

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class='section part'>
	<p> PASTA helps instructors assessing the skills of
	students and provides these students with instant feedback
	not exclusively based on their programming skills but also on
	their ability to design algorithms, understand network protocols, 
	test databases, reason logically, measure complexity,
	etc.
</div>

<div class='section'>
	<h1 class='section-title' style='text-align:center'>Login to PASTA</h1>
	<br />
	<form:errors path="loginForm.*">
		<div class="error"><span class="image"></span>
			<p class="message"><spring:message code="errors.message" /></p>
		</div>
	</form:errors>
	<form:form method="post" commandName="LOGINFORM" autocomplete="off">
		<div class='part' style="margin:0 auto; display:table;">
			<div class='pasta-form narrow'>
				<div class='pf-item one-col'>
					<div class='pf-label'>
						<form:label for="unikey" path="unikey" cssClass="required">UniKey <span class="star-required">*</span></form:label>
					</div>
					<div class='pf-input'>
						<form:input path="unikey" size="50" />
						<form:errors path="unikey" element="div" />
						<script>document.getElementById('unikey').focus()</script>
					</div>
				</div>
				<div class='pf-item one-col'>
					<div class='pf-label'>
						<form:label path="password" cssClass="required">Password <span class="star-required">*</span></form:label>
					</div>
					<div class='pf-input'>
						<form:password path="password" size="50" />
						<form:errors path="password" element="div" />
					</div>
				</div>
			</div>
			
			<div class='button-panel' style='text-align:center;'>
				<button type="submit" id="Submit" name="Submit">Login</button>
			</div>
		</div>
	</form:form>
</div>
