<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1>${unikey.username}</h1>

<table class="pastaQuickFeedback">
	<c:forEach var="result" items="${results}">
		<form:form commandName="submission" enctype="multipart/form-data" method="POST">
			<tr>
				<td style="width:40px;">
					<form:input type="file" path="file"/>
					<form:input type="hidden" path="assessment" value="${result.assessment.shortName}"/>
			    	<input type="submit" value="Upload" id="submit"/>
				</td>
				<td>
					<a href="../info/${result.assessment.name}/">${result.assessment.name}</a> - 
					<fmt:formatNumber type="number" maxIntegerDigits="3" value="${result.marks}" />
					/ ${result.assessment.marks}</br>
				${result.assessment.dueDate}</br>
				<c:choose>
					<c:when test="${result.assessment.numSubmissionsAllowed == 0}">
						&infin; sumbissions allowed </br>
					</c:when>
					<c:otherwise>
						${result.submissionsMade} of ${result.assessment.numSubmissionsAllowed} attempts made</br>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${result.submissionsMade == 0}">
						No attempts on record.
					</c:when>
					<c:when test="${result.compileError}">
						<div class="ui-state-error ui-corner-all" style="font-size: 1em;">
							<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
							<b>Compilation errors</b>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="allUnitTests" items="${result.unitTests}">
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