<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<h1>Login</h1>
<br />
<div class="susk-form" style="text-align:center; width:500px">
	<form:errors path="loginForm.*">
		<div class="susk-info-bar error"><span class="image"></span>
			<p class="message"><spring:message code="errors.message" /></p>
		</div>
	</form:errors>
	<form:form method="post" commandName="LOGINFORM" autocomplete="off">
		<div>
			<form:label for="unikey" path="unikey" cssClass="required">UniKey <span class="star-required">*</span></form:label>
			<form:input path="unikey" size="50"  type="text" name="unikey" id="unikey" />
			<form:errors path="unikey" cssClass="susk-form-errors" element="div" />
			<script>document.getElementById('unikey').focus()</script>
		</div>
		<div class="susk-form-clear"></div>
		<div>
			<form:label path="password" cssClass="required">Password <span class="star-required">*</span></form:label> 
			<form:password path="password" size="50" name="password" id="password" />
			<form:errors path="password" cssClass="susk-form-errors" element="div" />
		</div>
		<div class="susk-form-clear"></div>
		
		<div style="text-align:left">
			<button type="submit" style= "margin-left: 17.5em; padding-left: 1em;padding-right: 1em;"id="Submit" name="Submit">Login</button>
		</div>
		<div class="susk-form-clear"></div>
	</form:form>
	<div class="susk-form-clear"></div>
</div>