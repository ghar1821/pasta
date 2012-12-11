<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<table>
	<tr>
		<th>Username</th>
		<th>Stream</th>
		<th>Class</th>
		<c:forEach var="assessment" items="${assessmentList}">
			<th>${assessment.name}</th>
		</c:forEach>
	</tr>
	<c:forEach var="user" items="${userList}">
		<c:if test="${not user.tutor}">
			<tr>
				<td>${user.username}</td>
				<td>${user.stream}</td>
				<td>${user.tutorial}</td>
				<c:forEach var="assessment" items="${assessmentList}">
					<td>${latestResults[user.username][assessment.name].marks}</td>
				</c:forEach>
			</tr>
		</c:if>
	</c:forEach>
</table>