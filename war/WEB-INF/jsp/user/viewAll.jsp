<!-- 
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
-->

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
		onclick="window.location = '../downloadMarks/'">Download Marks</button>
</div>
<div style="float: left; width:100%">
	<button style="float: left; text-align: center;"
		onclick="window.location = '../downloadAutoMarks/'">Download Auto Marks ONLY</button>
</div>

<style>
	th, td { white-space: nowrap; }
	div.dataTables_wrapper {
		width: 800px;
		margin: 0 auto;
	}
</style>

<table id="gradeCentreTable"  class="display" cellspacing="0" width="100%">
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
					<td><a href="../student/${user.username}/home/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">${user.username}</a></td>
					<td><a href="../stream/${user.stream}/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">${user.stream}</a></td>
					<td><a href="../tutorial/${user.tutorial}/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">${user.tutorial}</a></td>
					<c:forEach var="assessment" items="${assessmentList}">
						<td class="gradeCentreMark" >
							<a href="../student/${user.username}/info/${assessment.shortName}/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">
							<span style="display:none">${latestResults[user.username][assessment.shortName].percentage}</span>
							<fmt:formatNumber type="number" maxIntegerDigits="3" value="${latestResults[user.username][assessment.shortName].marks}" />
							</a>
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
			var oTable = $('#gradeCentreTable').dataTable({
				"scrollX": true,
				"iDisplayLength": 25
			} );
	    } 
	); 

	var clr = $("div.gradeCentreMarkGood").css("backgroundColor").replace("rgb(","").replace(")","");
	var yr = parseFloat(clr.split(",")[0]);
	var yg = parseFloat(clr.split(",")[1]);
	var yb = parseFloat(clr.split(",")[2]);
		
	var clrBad = $("div.gradeCentreMarkBad").css("backgroundColor").replace("rgb(","").replace(")","");
	var xr = parseFloat(clrBad.split(",")[0]);
	var xg = parseFloat(clrBad.split(",")[1]);
	var xb = parseFloat(clrBad.split(",")[2]);
	
	$("td.gradeCentreMark").each(function(){
	
		// change them according to the percentage
		if(this.getElementsByTagName("span")[0].innerHTML == ""){
			$(this).css({backgroundColor:$("div.gradeCentreMarkNoSub").css("backgroundColor")});
		}
		else{
			var pos = parseFloat(this.getElementsByTagName("span")[0].innerHTML);
			
			n = 100; // number of color groups
			
			red = parseInt((xr + (( pos * (yr - xr)))).toFixed(0));
			green = parseInt((xg + (( pos * (yg - xg)))).toFixed(0));
			blue = parseInt((xb + (( pos * (yb - xb)))).toFixed(0));

			$(this).css({backgroundColor:'rgb('+red+','+green+','+blue+')'});
		}
			
	});
</script>
