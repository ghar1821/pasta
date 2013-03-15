<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Ignore me, I don't exist -->
<div style="display:none" class="gradeCentreMarkGood"></div>
<div style="display:none" class="gradeCentreMarkBad"></div>
<div style="display:none" class="gradeCentreMarkNoSub"></div>

<div style="float: left; width:100%">
	<button style="float: left; text-align: center;"
		onclick="$('#graphs').slideToggle('fast')">Toggle graphs</button>
</div>
<div style="float: left; width:100%">
	<button style="float: left; text-align: center;"
		onclick="window.location = '../downloadMarks/'">Download Marks</button>
</div>

<div id="graphs">
	<h1 style="margin-bottom:0.5em;">${assessment.name}</h1>
	<h3>Mark distribution</h3>
	<div id="markDistribution" style="height:300px; width:90%;"></div>
	<h3>Number of submissions distribution</h3>
	<div id="submissionDistribution" style="height:300px; width:90%;"></div>
	
	
	<script>
	$(document).ready(function(){
		
		var plot1 = $.jqplot ('markDistribution', [
		    <c:forEach var="assessment" items="${assessments}" varStatus="assessmentStatus">
			    <c:if test="${not (assessmentStatus.index == 0)}">
				,
				</c:if>
		    	[
				<c:forEach var="mark" items="${markDistribution[assessment.shortName]}" varStatus="markStatus">
					<c:if test="${not (markStatus.index == 0)}">
						,
					</c:if>
					[${markStatus.index/maxBreaks*100}, ${mark}]
				</c:forEach>
		    	 ]
		    </c:forEach>
	   		], {
		   		axesDefaults: {
		   			pad: 0
		   		},
		   		axes: {
		   			xaxis:{
		   				label:'Percentage (%)'
		   			},
		   			yaxis:{
		   				label:'Number of students'
		   			}
		   		},
		   		series:[
					<c:forEach var="assessment" items="${assessments}" varStatus="assessmentStatus"><c:if test="${not (assessmentStatus.index == 0)}">,</c:if>{label: '${assessment.name}'}</c:forEach>
		   		        ],
		   		     legend: {
		   	            show: true,
		   	            placement: 'outsideGrid'
		   	        }
			}
		);
		
		
		var plot2 = $.jqplot ('submissionDistribution', [
		    <c:forEach var="assessment" items="${assessments}" varStatus="assessmentStatus">
		      	<c:if test="${not (assessmentStatus.index == 0)}">
				,
				</c:if>
		    	[
				<c:forEach var="dist" items="${submissionDistribution[assessment.shortName]}" varStatus="distStatus">
					<c:if test="${not (distStatus.index == 0)}">
						,
					</c:if>
						[${dist.key}, ${dist.value}]
				</c:forEach>
		    	 ]
		    </c:forEach> 
		         ], {
	   		axesDefaults: {
	   			pad: 0
	   		},
	   		axes: {
	   			xaxis:{
	   				label:'Number of submissions'
	   			},
	   			yaxis:{
	   				label:'Number of students'
	   			}
	   		},
	   		series:[
				<c:forEach var="assessment" items="${assessments}" varStatus="assessmentStatus"><c:if test="${not (assessmentStatus.index == 0)}">,</c:if>{label: '${assessment.name}'}</c:forEach>
	   		        ],
	   		     legend: {
	   	            show: true,
	   	            placement: 'outsideGrid'
	   	        }
		}
	);
	});
	</script>
</div>

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
						<td class="gradeCentreMark"  onClick="window.location.href=window.location.href+'../student/${user.username}/info/${assessment.shortName}/'">
							<span style="display:none">${latestResults[user.username][assessment.shortName].percentage}</span>
							<fmt:formatNumber type="number" maxIntegerDigits="3" value="${latestResults[user.username][assessment.shortName].marks}" />
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
			$('#graphs').css('display', 'none');
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