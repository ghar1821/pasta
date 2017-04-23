<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<%@ attribute name="rule" type="pasta.domain.release.ReleaseRule" required="true"%>
<%@ attribute name="topLevel" required="false" type="java.lang.Boolean" %>

<c:set var="ruleName" value="${rule.name}" />
<c:if test="${empty topLevel}">
	<c:set var="topLevel" value="true" />
</c:if>

<c:if test="${topLevel}">
	<div class='release-rule'>
	Release when
</c:if>
<c:choose>
	<c:when test="${empty rule or empty ruleName}">
		[no rule]
	</c:when>
	
	<c:when test="${fn:endsWith(ruleName, 'ClassRule')}">
		the user is in one of the following classes: ${rule.classes}
	</c:when>
	
	<c:when test="${fn:endsWith(ruleName, 'DateRule')}">
		the date is at or after <pasta:readableDate date="${rule.releaseDate}" />
	</c:when>
	
	<c:when test="${fn:endsWith(ruleName, 'HasSubmittedRule')}">
		the user has submitted "${rule.compareAssessment.name}"
	</c:when>		
	
	<c:when test="${fn:endsWith(ruleName, 'MarkCompareRule')}">
		the user has a mark (${rule.markType.text}) ${rule.compareMode.text} <c:choose>
		<c:when test="${rule.asPercentage}"><fmt:formatNumber type="percent" value="${rule.compareMark}" /></c:when>
		<c:when test="${not rule.asPercentage}">${rule.compareMark}</c:when>
		</c:choose>
		for "${rule.compareAssessment.name}"
	</c:when>
	
	<c:when test="${fn:endsWith(ruleName, 'ReleaseAndRule') or fn:endsWith(ruleName, 'ReleaseOrRule')}">
		<c:set var="firstOne" value="true" />
		<c:set var="conjunction" value="all" />
		<c:if test="${fn:endsWith(ruleName, 'ReleaseOrRule')}"><c:set var="conjunction" value="at least one" /></c:if>
		<strong>${conjunction}</strong> of the following rules are met:
		
		<ul <c:if test="${topLevel}">class='first'</c:if>>
			<c:forEach var="subrule" items="${rule.rules}" varStatus="ruleStatus">
				<li><tag:releaseRulePrinted topLevel="false" rule="${subrule}" />
				<c:if test="${firstOne}"><c:set var="firstOne" value="false" /></c:if>
			</c:forEach>
		</ul>
		<c:if test="${empty rule.rules}">
			[no sub rules]
		</c:if>
	</c:when>		
	
	<c:when test="${fn:endsWith(ruleName, 'StreamRule')}">
		the user is in one of the following streams: ${rule.streams}
	</c:when>			
	
	<c:when test="${fn:endsWith(ruleName, 'SubmissionCountRule')}">
		the user has a submission count ${rule.compareMode.text} ${rule.submissionCount} for "${rule.compareAssessment.name}"
	</c:when>
	
	<c:when test="${fn:endsWith(ruleName, 'UsernameRule')}">
		the user is in this list: ${rule.usernames}
	</c:when>			
	
	<c:otherwise>
		[invalid rule - ${ruleName}]
	</c:otherwise>
</c:choose>
<c:if test="${topLevel}">
	</div>
	<script>
		$(".release-rule ul.first > li > ul > li > ul > li > ul").addClass("first");
	</script>
</c:if>