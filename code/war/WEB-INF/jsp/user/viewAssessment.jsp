<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1>${assessment.name}</h1>

<h3>Due: ${assessment.dueDate}</h3>
<h3>Worth: ${assessment.marks} marks</h3>

${assessment.description}

<c:if test="${not empty history}">
	<h3>History</h3>
	
	<c:forEach var="result" items="${history}">
		<h4>${result.submissionDate}</h4>
		<c:choose>
			<c:when test="${result.compileError}">
				<div class="ui-state-error ui-corner-all" style="font-size: 1em;">
					<pre>
						${result.compilationError}
					</pre>
				</div>
			</c:when>
			<c:otherwise>
				<c:forEach var="allUnitTests" items="${result.unitTests}">
					<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
						<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
					</c:forEach>
				</c:forEach>
				<table class="pastaTable">
					<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
					<c:forEach var="allUnitTests" items="${result.unitTests}">
						<c:forEach var="testCase" items="${allUnitTests.testCases}">
							<tr>
								<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
								<td style="text-align:left;">${testCase.testName}</td>
								<td>${testCase.time}</td>
								<td>
									<pre>${testCase.type} - ${testCase.testMessage}</pre>
								</td>
							</tr>
						</c:forEach>
					</c:forEach>
				</table>
			</c:otherwise>
		</c:choose>
	</c:forEach>
</c:if>