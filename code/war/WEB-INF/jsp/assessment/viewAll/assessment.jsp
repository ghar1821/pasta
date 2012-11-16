<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Assessments</h1>

<table class="pastaTable">
	<tr><th>Name</th><th>Due Date</th><th>Marks</th><th># Submissions Allowed</th><th># Public Unit Tests</th><th># Secret Unit Tests</th><th># Hand Marking</th><th># Competitions</th></tr>
	<c:forEach var="assessment" items="${allAssessments}">
		<tr>
			<td><a href="../view/${assessment.shortName}/">${assessment.name}</a></td>
			<td>${assessment.dueDate}</td><td>${assessment.marks}</td>
			<td>${assessment.numSubmissionsAllowed > 0 ? assessment.numSubmissionsAllowed : '&infin;'}</td>
			<td>${fn:length(assessment.unitTests)}</td>
			<td>${fn:length(assessment.secretUnitTests)}</td>
			<td>TODO</td>
			<td>TODO</td>
		</tr>
	</c:forEach>
</table> 