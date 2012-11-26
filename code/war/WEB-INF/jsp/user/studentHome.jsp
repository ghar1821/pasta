<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1>${unikey}</h1>

<table class="pastaQuickFeedback">
	<c:forEach var="result" items="${results}">
		<tr>
			<td style="width:40px;">DnD + STATUS</td>
			<td>${result.assessment.name} - ${result.marks} / ${result.assessment.marks}</br>
			${result.assessment.dueDate}</br>
			<c:forEach var="allUnitTests" items="${result.unitTests}">
				<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
					<div class="pastaUnitTestResult${unitTestCase.testResult}" style="float:left;" title="${unitTestCase.testName}">&nbsp</div>
				</c:forEach>
			</c:forEach>
		</tr>
	</c:forEach>
</table>