<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<style type="text/css">
.datacellone {
	background-color: #CC9999; color: black;
}
.datacelltwo {
	background-color: #9999CC; color: black;
}
</style>


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
				<td><a href="../student/${user.username}/home/">${user.username}</a></td>
				<td><a href="../stream/${user.stream}/">${user.stream}</a></td>
				<td><a href="../tutorial/${user.tutorial}/">${user.tutorial}</a></td>
				<c:forEach var="assessment" items="${assessmentList}">
					<td class="gradeCentreMark">
						<fmt:formatNumber type="number" maxIntegerDigits="3" value="${latestResults[user.username][assessment.name].marks}" />
					</td>
				</c:forEach>
			</tr>
		</c:if>
	</c:forEach>
</table>