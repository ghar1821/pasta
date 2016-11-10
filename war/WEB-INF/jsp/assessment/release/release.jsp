
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<h1>Release Rule - ${assessment.name}</h1>
<spring:hasBindErrors name="releaseRuleForm">
	<div class="ui-state-error ui-corner-all vertical-block" style="font-size: 1.4em;">
		<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
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