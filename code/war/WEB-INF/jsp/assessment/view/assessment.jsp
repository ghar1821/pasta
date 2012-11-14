<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>${assessment.name}</h1>

<!--TODO make the values changable #45 -->
<table>
	<tr><td>Due Date: </td><td>${assessment.dueDate}</td></tr>
	<tr><td>Marks alloted: </td><td>${assessment.marks}</td></tr>
	<tr><td>Submissions Allowed: </td><td>${assessment.numSubmissionsAllowed > 0 ? assessment.numSubmissionsAllowed : '&infin;'}</td></tr> 
</table>

<c:if test="${not empty assessment.unitTests}">
	<h2> Unit Tests </h2>
	<table>
		<tr><th>Name</th><th>Weighting</th><th>Tested</th></tr>
		<c:forEach var="unitTest" items="${assessment.unitTests}">
			<tr><td>${unitTest.test.name}</td><td>${unitTest.weight}</td><td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td></tr>
		</c:forEach>
	</table>
</c:if>

<c:if test="${not empty assessment.secretUnitTests}">
	<h2> Secret Unit Tests </h2>
</c:if>

<!-- TODO add for hand marking and competitions # 46-->