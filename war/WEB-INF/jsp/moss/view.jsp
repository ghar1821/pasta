<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h2>Moss Result: ${mossResults.date}</h2>

<p>Link: <a href="${mossResults.link}">${mossResults.link}</a> <br/>

<table id="mossTable" class="tablesorter">
	<thead>
		<tr>
			<th>Student</th>
			<th>Percentage</th>
			<th>Student</th>
			<th>Percentage</th>
			<th>Lines</th>
			<th>Max Percentage</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="pairing" items="${mossResults.pairings}">
			<tr>
				<td>${pairing.student1}</td>
				<td>${pairing.percentage1}</td>
				<td>${pairing.student2}</td>
				<td>${pairing.percentage2}</td>
				<td>${pairing.lines}</td>
				<td>${pairing.maxPercentage}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<script>
$(document).ready(function() { 
        $("#mossTable").dataTable(); 
    } 
); 
</script>