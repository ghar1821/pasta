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

<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<h1>Release Rule - ${assessment.name}</h1>
<spring:hasBindErrors name="releaseRuleForm">
	<div class="ui-state-error ui-corner-all vertical-block" style="font-size: 1.4em;">
		<span class="ui-icon ui-icon-alert" style="margin-right: .3em;"></span>
		<b>ERROR:</b> Please check your form for indicated errors, and try again.
	</div>
</spring:hasBindErrors>

<div class='section'>
	<form:form commandName="releaseRuleForm">
		<div class='ruleParent first' pathPrefix='releaseRuleForm'>
			<tag:releaseRule rule="${releaseRuleForm}" pathPrefix="releaseRuleForm" />
		</div>
		<div class='button-panel'>
			<%-- Not a proper submit button as for some reason the button renders outside of the form --%>
			<button type='button' id='submitButton'>Submit</button>
		</div>
	</form:form>
</div>

<script src='<c:url value="/static/scripts/assessment/releaseRules.js"/>'></script>