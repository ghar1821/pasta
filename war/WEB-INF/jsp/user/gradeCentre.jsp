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

<h1>Grade Centre</h1>

<!-- Ignore me, I don't exist -->
<div style="display:none" class="gradeCentreMarkGood"></div>
<div style="display:none" class="gradeCentreMarkBad"></div>
<div style="display:none" class="gradeCentreMarkNoSub"></div>

<script>
	var clr = $("div.gradeCentreMarkGood").css("backgroundColor").replace("rgb(","").replace(")","");
	var yr = parseFloat(clr.split(",")[0]);
	var yg = parseFloat(clr.split(",")[1]);
	var yb = parseFloat(clr.split(",")[2]);
		
	var clrBad = $("div.gradeCentreMarkBad").css("backgroundColor").replace("rgb(","").replace(")","");
	var xr = parseFloat(clrBad.split(",")[0]);
	var xg = parseFloat(clrBad.split(",")[1]);
	var xb = parseFloat(clrBad.split(",")[2]);
</script>

<c:if test="${pathBack == null}">
	<c:set var="pathBack" value=".." />
</c:if>
<c:if test="${not empty stream}">
	<c:set var="streamQuery" value="stream=${stream}&" />
</c:if>
<c:if test="${not empty tutorial}">
	<c:set var="tutorialQuery" value="tutorial=${tutorial}&" />
</c:if>
<c:if test="${not empty myClasses}">
	<c:set var="myClassesQuery" value="myClasses=true&" />
</c:if>

<div class='section'>
	<div class='part'>
		<table id="gradeCentreTable" class="display compact">
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
		</table>
	</div>
	<div class='button-panel'>
		<button class='flat' onclick="window.location = '${pathBack}/downloadMarks/?${myClassesQuery}${tutorialQuery}${streamQuery}'">Download Marks</button>
		<button class='flat' onclick="window.location = '${pathBack}/downloadAutoMarks/?${myClassesQuery}${tutorialQuery}${streamQuery}'">Download Auto Marks ONLY</button>
	</div>
</div>

<script>
	$(document).ready(function() 
	    { 			
			var oTable = $('#gradeCentreTable').dataTable({
				"scrollX": true,
				"iDisplayLength": 25,
				"ajax": "DATA/",
				"deferRender": true,
		        "columns": [
					{ "data": "name" },
					{ "data": "stream" },
					{ "data": "class" },
					<c:forEach var="assessment" items="${assessmentList}" varStatus="assessmentStatus">
					{ "data": {_: "${assessment.id}", sort: "${assessment.id}.percentage"}}<c:if test="${not assessmentStatus.last}">,</c:if>
					</c:forEach>
		         ],
				"columnDefs": [ 
					{
						"targets" : 0,
						"render" : function(data) {
							return '<a class="gc-link" href="${pathBack}/student/'+data+'/home/">'+data+'</a>';
						},
						"createdCell" : null
					},{
						"targets" : 1,
						"render" : function(data) {
							return '<a class="gc-link" href="${pathBack}/stream/'+data+'/">'+data+'</a>';
						},
						"createdCell" : null
					},{
						"targets" : 2,
						"render" : function(data) {
							return '<a class="gc-link" href="${pathBack}/tutorial/'+data+'/">'+data.substring(data.indexOf('.')+1)+'</a>';
						},
						"createdCell" : null
					},{
						"targets": "_all",
						"render": function(data, type, row, meta) {
							if(type === 'display') {
								if(data.mark) {
									return '<a class="gc-link" href="${pathBack}/student/'+row.name+'/info/'+data.assessmentid+'/">'+data.mark+'</a>'
								}
								return null;
							}
							return data;
						},
						"createdCell" : function(td, cellData, rowData, row, col) {
							if(cellData.mark === "") {
								$(td).css('background-color', $("div.gradeCentreMarkNoSub").css("background-color"));
								return;
							}
							// adjust 0 out of 0 to display as full marks
							var percentage = cellData.mark == 0 && cellData.max == 0 ? 1.0 : cellData.percentage;
							red = parseInt((xr + (( percentage * (yr - xr)))).toFixed(0));
							green = parseInt((xg + (( percentage * (yg - xg)))).toFixed(0));
							blue = parseInt((xb + (( percentage * (yb - xb)))).toFixed(0));
							$(td).css('background-color', 'rgb('+red+','+green+','+blue+')');
						}
					}
				],
				"language": {
				    "emptyTable": "No students to display."
				}
			} );
			
			if(!location.href.endsWith("/gradeCentre/")) {
				$(".button-panel").prepend(
						$("<button/>")
							.addClass("flat")
							.text("Full Grade Centre")
							.on("click", function(){location.href = "<c:url value='/gradeCentre/'/>"})
				);
			}
	    } 
	); 

</script>