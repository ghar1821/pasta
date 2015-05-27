
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<h1>Release Rule - ${assessment.name}</h1>
<div style='font-size: 1.1em; padding-bottom:20em'>
	<form:form commandName="releaseRuleForm">
		<div>
			<div class='boxCard vertical-block ruleParent first' pathPrefix='releaseRuleForm'>
				<tag:releaseRule rule="${releaseRuleForm}" pathPrefix="releaseRuleForm" />
			</div>
			<button type='submit'>Submit</button>
		</div>
	</form:form>
</div>

<script src='<c:url value="/static/scripts/assessment/releaseRules.js"/>'></script>