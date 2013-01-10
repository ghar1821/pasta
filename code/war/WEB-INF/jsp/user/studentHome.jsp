<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:choose>
	<c:when test="${ not empty viewedUser}">
		<h1>${viewedUser.username}</h1>
	</c:when>
	<c:otherwise>
		<h1>${unikey.username}</h1>
	</c:otherwise>
</c:choose>

<table class="pastaQuickFeedback">
	<c:forEach var="assessment" items="${assessments}">
		<form:form commandName="submission" enctype="multipart/form-data" method="POST">
			<tr>
				<td style="width:40px;">
					<form:input type="file" path="file"/>
					<form:input type="hidden" path="assessment" value="${assessment.shortName}"/>
			    	<input type="submit" value="Upload" id="submit"/>
				</td>
				<td>
					<a href="../info/${assessment.name}/">${assessment.name}</a> - 
					<fmt:formatNumber type="number" maxIntegerDigits="3" value="${results[assessment.shortName].marks}" />
					<c:if test="${empty results[assessment.shortName]}">
						0
					</c:if>
					/ ${assessment.marks}</br>
				${assessment.dueDate}</br>
				<c:choose>
					<c:when test="${assessment.numSubmissionsAllowed == 0}">
						&infin; sumbissions allowed </br>
					</c:when>
					<c:otherwise>
						<c:if test="${empty results[assessment.shortName]}">
							0
						</c:if>
						${results[assessment.shortName].submissionsMade} of ${results[assessment.shortName].assessment.numSubmissionsAllowed} attempts made</br>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${results[assessment.shortName].submissionsMade == 0 or empty results[assessment.shortName]}">
						No attempts on record.
					</c:when>
					<c:when test="${results[assessment.shortName].compileError}">
						<div class="ui-state-error ui-corner-all" style="font-size: 1em;">
							<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
							<b>Compilation errors</b>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="allUnitTests" items="${results[assessment.shortName].unitTests}">
							<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
								<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
							</c:forEach>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</tr>
		</form:form>
	</c:forEach>
</table>