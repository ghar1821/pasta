<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Ignore me, I don't exist -->
<div style="display:none" class="gradeCentreMarkGood"></div>
<div style="display:none" class="gradeCentreMarkBad"></div>
<div style="display:none" class="gradeCentreMarkNoSub"></div>

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
					<td onClick="window.location.href=window.location.href+'../student/${user.username}/home/'">${user.username}</td>
					<td onClick="window.location.href=window.location.href+'../stream/${user.stream}/'">${user.stream}</td>
					<td onClick="window.location.href=window.location.href+'../tutorial/${user.tutorial}/'">${user.tutorial}</td>
					<c:forEach var="assessment" items="${assessmentList}">
						<td class="gradeCentreMark"  onClick="window.location.href=window.location.href+'../student/${user.username}/info/${assessment.name}/'">
							<fmt:formatNumber type="number" maxIntegerDigits="3" value="${latestResults[user.username][assessment.name].marks}" />
							<span style="display:none">${latestResults[user.username][assessment.name].percentage}</span>
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
			var oTable = $('#gradeCentreTable').dataTable( {
		 		"sScrollX": "100%",
		 		"sScrollXInner": "150%",
		 		"bScrollCollapse": true,
		 		"iDisplayLength": 25
		 	} );
		 	new FixedColumns( oTable, {
		 		"iLeftColumns": 3,
				"iLeftWidth": 250
		 	} );
	    } 
	); 
	
	$("td.gradeCentreMark").each(function(){
	
		var clr = $("div.gradeCentreMarkGood").css("backgroundColor").replace("rgb(","").replace(")","");
		var yr = parseFloat(clr.split(",")[0]);
		var yg = parseFloat(clr.split(",")[1]);
		var yb = parseFloat(clr.split(",")[2]);
		
		var clrBad = $("div.gradeCentreMarkBad").css("backgroundColor").replace("rgb(","").replace(")","");
		var xr = parseFloat(clrBad.split(",")[0]);
		var xg = parseFloat(clrBad.split(",")[1]);
		var xb = parseFloat(clrBad.split(",")[2]);
		
		// change them according to the percentage
		if(this.getElementsByTagName("span")[0].innerHTML == ""){
			clr = $("div.gradeCentreMarkNoSub").css("backgroundColor");
		}
		else{
			var pos = parseFloat(this.getElementsByTagName("span")[0].innerHTML);
			
			n = 100; // number of color groups
			
			red = parseInt((xr + (( pos * (yr - xr)))).toFixed(0));
			green = parseInt((xg + (( pos * (yg - xg)))).toFixed(0));
			blue = parseInt((xb + (( pos * (yb - xb)))).toFixed(0));
			clr = 'rgb('+red+','+green+','+blue+')';
		}
		$(this).css({backgroundColor:clr});
				
	});
</script>