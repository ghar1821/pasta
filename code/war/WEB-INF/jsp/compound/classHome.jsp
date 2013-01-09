<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1>${classname}</h1>

<c:forEach var="assessment" items="${assessmentList}">
	${assessment.name} ${statistics[assessment.name]["max"]}
</c:forEach>

<table id="gradeCentreTable" class="tablesorter">
	<thead>
		<tr>
			<th>Username</th>
			<th>Stream</th>
			<th>Class</th>
			<c:forEach var="assessment" items="${assessmentList}">
				<th>${assessment.name}</th>
			</c:forEach>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="user" items="${userList}">
			<c:if test="${not user.tutor}">
				<tr>
					<td onClick="window.location.href=window.location.href+'../../student/${user.username}/home/'">${user.username}</td>
					<td onClick="window.location.href=window.location.href+'../../stream/${user.stream}/'">${user.stream}</td>
					<td onClick="window.location.href=window.location.href+'../../tutorial/${user.tutorial}/'">${user.tutorial}</td>
					<c:forEach var="assessment" items="${assessmentList}">
						<td class="gradeCentreMark"  onClick="window.location.href=window.location.href+'../student/${user.username}/info/${assessment.name}/'">
							<fmt:formatNumber type="number" maxIntegerDigits="3" value="${latestResults[user.username][assessment.name].marks}" />
						</td>
					</c:forEach>
				</tr>
			</c:if>
		</c:forEach>
	</tbody>
</table>

<script>
	$(document).ready(function() 
	    { 
	        $("table").tablesorter( {sortList: [[0,0], [1,0]]} );  
	    } 
	); 
</script>