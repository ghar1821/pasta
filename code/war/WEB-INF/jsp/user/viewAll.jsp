<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<table id="gradeCentreTable" class="tablesorter">
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
				<td href="../student/${user.username}/home/">${user.username}</td>
				<td href="../stream/${user.stream}/">${user.stream}</td>
				<td href="../tutorial/${user.tutorial}/">${user.tutorial}</td>
				<c:forEach var="assessment" items="${assessmentList}">
					<td class="gradeCentreMark"  href="../student/${user.username}/info/${assessment.name}/">
						<fmt:formatNumber type="number" maxIntegerDigits="3" value="${latestResults[user.username][assessment.name].marks}" />
					</td>
				</c:forEach>
			</tr>
		</c:if>
	</c:forEach>
</table>

<script>
	$(document).ready(function() 
	    { 
	        $("table").tablesorter( {sortList: [[0,0], [1,0]]} );  
	        alert("shit went down!!!!! :C");
	    } 
	); 
</script>