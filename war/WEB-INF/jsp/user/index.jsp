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
<h1>${unikey}</h1>
<c:if test="${user.tutor}">
	<a href="downloadall/${unikey}">Download all submissions</a>
</c:if>
<c:if test="${not empty assessmentList}">
	<spring:hasBindErrors name="submission">
		<div class="ui-state-error" style="font-size:20">
			<form:errors path="submission" cssClass="susk-form-errors" element="div" />
		</div>
	</spring:hasBindErrors>
	<table>
	<tr>
		<th>Status</th>
		<th>Name</th>
		<th>Due Date</th>
		<th>Result</th>
		<th>Submit</th>
		<th>History</th>
	</tr>
	<c:forEach var="assessment" items="${assessmentList}">
		<tr>
		<td style="width: 17px; height: 17px; ${allAssessments[assessment].colorCode}">
			<c:if test="${allAssessments[assessment].pastDueDate}">
				<span class="ui-icon ui-icon-locked" style="display: block; width:16px; height:16px; float:right"></span>
			</c:if>
		</td> <!-- green for submitted and some tests working, yellow for processing, red for submitted and did not compile : lock for past due date, no lock otherwise -->
		<td>${allAssessments[assessment].name}</td>
		<td>${allAssessments[assessment].dueDate}</td>
		<td>${allAssessments[assessment].result}</td>
		<td style="width: 17px; height: 17px"><span onclick="document.getElementById('assessmentName').value = '${allAssessments[assessment].name}';popup();" title="Submit" class="ui-icon ui-icon-arrowreturnthick-1-e" style="cursor: pointer; display: block; width: 16px; height: 16px; float:right"></span></td>
		<td style="width: 17px; height: 17px"><span onclick="window.location.href=window.location.href+'/submission/${allAssessments[assessment].name}'" class="ui-icon ui-icon-script" title="History" style="cursor: pointer; display: block; width: 16px; height: 16px; float:right"></span></td>
		</tr>
	</c:forEach>
	</table>
</c:if>

<c:if test="${user.tutor}">
	<a href="all">View all students</a>
</c:if>

<div id="dialog-box" style="display:none; width:350px; height:70px; position:fixed; padding:10px"">
	<div onclick="popdown()" style="width: 20px; height: 20px; float:left; position: absolute; right:0em; top:0em; background:#CA5555; -moz-border-top-right-radius: 5px;-webkit-border-top-right-radius: 5px;-moz-border-bottom-left-radius: 5px;-webkit-border-bottom-left-radius: 5px;">	
		<span class="ui-icon ui-icon-closethick" title="Exit" style="cursor: pointer; display: block; width: 16px; height: 16px; float:left;background-image: url(../static/styles/jquery/smoothness/images/ui-icons_ffffff_256x240.png);"></span>
	</div>
	<div style="position: absolute">
	<h1>Please upload a file</h1>
    <form:form commandName="submission" enctype="multipart/form-data" method="POST">
    	<form:input type="file" path="file" />
    	<c:choose>
    		<c:when test="${user.tutor}">
    			<br />STUDENT: 
    			<form:input type="text" path="unikey" value="${user.unikey}"/>
    		</c:when>
    		<c:otherwise>
    			<form:input type="hidden" path="unikey" value="${user.unikey}"/>
    		</c:otherwise>
    	</c:choose> 
    	<form:input type="hidden" path="assessmentName" value=""/>
    	<input type="submit" value="Upload File"/>
	</form:form>
	</div>
</div>
<div id="dialog-overlay"></div>
<script type="text/javascript">
	
	function popup() {
		
		// get the screen height and width  
		var maskHeight = $(document).height();  
		var maskWidth = $(window).width();

		if(maskWidth < 998){
			maskWidth = 998;
		}

		var sideWidth = (maskWidth-998)/2 + 21;

		if(sideWidth < 0){
			sideWidth = 0;
		}
		
		// calculate the values for center alignment
		var dialogTop =  (maskHeight/3) - ($('#dialog-box').height());  
		var dialogLeft = (maskWidth/2) - ($('#dialog-box').width()/2); 
		
		// assign values to the overlay and dialog box
		$('#dialog-overlay').css({height:maskHeight-165, width:maskWidth-sideWidth}).fadeIn("slow");
		$('#dialog-overlay1').css({height:165, width:maskWidth}).fadeIn("slow");
		$('#dialog-overlay2').css({top:165, height:maskHeight-165, width:sideWidth}).fadeIn("slow");
		$('#dialog-box').css({top:dialogTop, left:dialogLeft}).fadeIn("slow"); 
		$('#tabbar').css('z-index',5);
	}
	
	function popdown() {		
		// assign values to the overlay and dialog box
		$('#dialog-overlay').fadeOut("slow");
		$('#dialog-overlay1').fadeOut("slow");
		$('#dialog-overlay2').fadeOut("slow");
		$('#dialog-box').fadeOut("slow"); 
		setTimeout("$('#tabbar').css('z-index',9);",500);
	}
	
</script>

<style type="text/css">
button{
	cursor: pointer;
	clear: both;
	border: 1px solid #DBD3C2;
	font-weight: bold;
	background-color: #CE1126;
	line-height: 2em;
	height:35px;	
	color:white;
}
button:hover {
	border-color: #DBD3C2;
	background-color: #12416C;
}

#dialog-overlay {

	/* set it to fill the whole screen */
	width:100%; 
	height:100%;
	
	/* transparency for different browsers */
	filter:alpha(opacity=50); 
	-moz-opacity:0.5; 
	-khtml-opacity: 0.5; 
	opacity: 0.5; 
	background:#000; 
	margin:auto;

	/* make sure it appear behind the dialog box but above everything else */
	position:absolute; 
	top:0; left:0; 
	z-index:3000; 

	/* hide it by default */
	display:none;
}


#dialog-box {
	
	/* css3 drop shadow */
	-webkit-box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.5);
	-moz-box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.5);
	
	/* css3 border radius */
	-moz-border-radius: 5px;
    -webkit-border-radius: 5px;
	
	background:#eee;
	/* styling of the dialog box, i have a fixed dimension for this demo */ 
	width:328px; 
	
	/* make sure it has the highest z-index */
	position:absolute; 
	z-index:5000; 

	/* hide it by default */
	display:none;
}

#dialog-box .dialog-content {
	/* style the content */
	text-align:left; 
	padding:10px; 
	margin:13px;
	color:#666; 
	font-family:arial;
	font-size:11px; 
}

/* extra styling */
#dialog-box .dialog-content p {
	font-weight:700; margin:0;
}

#dialog-box .dialog-content ul {
	margin:10px 0 10px 20px; 
	padding:0; 
	height:50px;
}

</style> 