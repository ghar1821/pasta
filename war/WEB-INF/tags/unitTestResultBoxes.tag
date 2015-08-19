<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="tests" required="true" type="java.util.List" rtexprvalue="true"%>
<%@ attribute name="closedAssessment" required="true" type="Boolean" rtexprvalue="true"%>

<div class='float-container'>
	<c:set var='anyResults' value='false' />
	<c:forEach var="testResult" items="${tests}">
		<c:set var="secret" value="${testResult.secret}"/>
		<c:set var="revealed" value="${secret && (user.tutor or closedAssessment)}"/>
		
		<c:forEach var="testCase" items="${testResult.testCases}">
			<c:set var='anyResults' value='true' />
			<div class="unitTestResult <c:if test="${not secret or revealed}">${testCase.testResult}</c:if>
			<c:if test="${revealed}">revealed</c:if>
			<c:if test="${secret}">secret</c:if>" 
			title="<c:choose><c:when test="${revealed or not secret}">${testCase.testName}</c:when><c:otherwise>???</c:otherwise></c:choose>">&nbsp;</div>
		</c:forEach>
	</c:forEach>
	<c:if test="${!anyResults}">
		No results available.
	</c:if>
</div>