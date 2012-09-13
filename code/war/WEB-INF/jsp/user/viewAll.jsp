<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<a href="all.xls">Download marks</a>
<a href="downloadall">Download all submissions</a>

<table id="allMarks" class="heat-map tablesorter">
	<thead>
		<tr>
			<th class="first">Unikey</th>
			<th>Tutorial Stream</th>
			<c:forEach var="assessmentName" items="${allAssessments}">
				<th>${assessmentName}</th>
			</c:forEach>
			<th class="last">Total</th>
		</tr>
	</thead>
	<tbody>
	<c:forEach var="entry" items="${allStudents}">
		<c:if test="${not entry.key.tutor}">
			<c:set var="sum" value="0" />
			<tr class="stats-row">
				<td class="stats-title"><a href="${entry.key.unikey}">${entry.key.unikey}</a></td>
				<td class="stats-title"><a href="downloadClass/${entry.key.tutorialClass}">${entry.key.tutorialClass}</a></td>
				<c:forEach var="assessmentName" items="${allAssessments}">
					<c:choose>
						<c:when test="${empty entry.value[assessmentName]}">
							<td class="percentage"><a href="${entry.key.unikey}/submission/${assessmentName}">N/A</a></td>
						</c:when>
						<c:otherwise>
							<td class="percentage"><a href="${entry.key.unikey}/submission/${entry.value[assessmentName].name}">${entry.value[assessmentName].percentage}</a></td>
							<c:set var="sum" value="${sum + entry.value[assessmentName].weightedMark}" />
						</c:otherwise>
					</c:choose>
				</c:forEach>
				<td><fmt:formatNumber value="${sum}" pattern="0.000"/></td>
		  	</tr>
		  </c:if>
	</c:forEach>
	</tbody>
</table>

<script type="text/JavaScript">
$(document).ready(function(){
 	
    // Loop through each data point and calculate its % value
    $('.heat-map tbody td').not('.stats-title').each(function(){
		// run max value function and store in variable
		var max = 1;
	 
		n = 100; // Declare the number of groups
	 
		// Define the ending colour, which is white
		xr = 255; // Red value
		xg = 255; // Green value
		xb = 255; // Blue value
	 
		// Define the starting colour #f32075
		yr = 32; // Green value
		yg = 117; // Blue value
		yb = 243; // Red value
		
		if($(this).text() == "DNC"){
			clr = 'red';
			$(this).css({backgroundColor:clr});
		}
		else if($(this).text() == "N/A"){
			clr = 'yellow';
			$(this).css({backgroundColor:clr});
		}
		else{
			var val = parseFloat($(this).text());
			var pos = parseFloat((Math.round((val/max)*100)).toFixed(0));
			red = parseInt((xr + (( pos * (yr - xr)) / (n-1))).toFixed(0));
			green = parseInt((xg + (( pos * (yg - xg)) / (n-1))).toFixed(0));
			blue = parseInt((xb + (( pos * (yb - xb)) / (n-1))).toFixed(0));
			clr = 'rgb('+red+','+green+','+blue+')';
			$(this).css({backgroundColor:clr});
		}
    });
	
	$('.heat-map tbody td').not('.percentage').each(function(){
		// run max value function and store in variable
		var max = 15;
	 
		n = 100; // Declare the number of groups
	 
		// Define the ending colour, which is white
		xr = 255; // Red value
		xg = 255; // Green value
		xb = 255; // Blue value
	 
		// Define the starting colour #f32075
		yb = 32; // Blue value
		yr = 117; // Red value
		yg = 243; // Green value
		
		var val = parseFloat($(this).text());
		var pos = parseFloat((Math.round((val/max)*100)).toFixed(0));
		red = parseInt((xr + (( pos * (yr - xr)) / (n-1))).toFixed(0));
		green = parseInt((xg + (( pos * (yg - xg)) / (n-1))).toFixed(0));
		blue = parseInt((xb + (( pos * (yb - xb)) / (n-1))).toFixed(0));
		clr = 'rgb('+red+','+green+','+blue+')';
		$(this).css({backgroundColor:clr});

    });
	
	$("#allMarks").tablesorter(); 
});
</script>