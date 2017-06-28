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
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<c:set var="owner" value="${assessmentName} - ${student.username}" />
<c:if test="${student.group}">
	<c:set var="owner" value="${student.name}" />
</c:if>

<h1>${owner}</h1>

<div class='section'>
	<c:if test="${student.group}">
		<div class='part no-line'>
			<h3 class='part-title'>Group Members:</h3>
			<ul>
			<c:forEach var="member" items="${studentsInGroup}">
				<li><a href='../../../../student/${member.username}/home/'>${member.username}</a>
				- ${member.tutorial}
				<c:if test="${not empty studentsInGroupExtensions[member.username]}">
					- extension to ${studentsInGroupExtensions[member.username]}
				</c:if>
			</c:forEach>
			</ul>
		</div>
	</c:if>
	<div class='part no-line'>
		<h3 class='part-title'>Submitted:</h3>
		<pasta:readableDate date="${assessmentResult.submissionDate}" /><c:out value="${lateString}"/>
	</div>
	<div class='part no-line'>
		<h3 class='part-title'>Submission:</h3>
		<jsp:include page="../../recursive/fileWriterRoot.jsp">
			<jsp:param name="owner" value="${owner}"/>
		</jsp:include>
	</div>
</div>

<form:form commandName="assessmentResult" enctype="multipart/form-data" method="POST">
	<div class='section'>
		<h3 class='section-title'>Automatic Marking Results</h3>
		<div class='part no-line'>
			<h4 class='part-title'>Summary</h4>
			<tag:unitTestResult closedAssessment="false" results="${assessmentResult}" summary="true" />
		</div>
		<div class='button-panel'>
			<button class='flat' type='button' id='detailsToggle'>Show Details</button>
		</div>			
		<div id="${assessmentResult.id}" class='resultDetails part'>
			<tag:unitTestResult closedAssessment="false" results="${assessmentResult}" />
		</div>
	</div>
	<div class='section'>
		<tag:handMarkingResult results="${assessmentResult}" marking="true" heading="Hand Marking Guidelines" />
	</div>
	
	<div class='section'>
		<h3 class='section-title'>Comments</h3>
		<div class='part no-line'>
			<form:textarea style="height:200px; width:100%" path="comments"/>
		</div>
		<div class='button-panel'>
			<button type="submit">Save Changes</button>
		</div>
	</div>
</form:form>


<script src='<c:url value="/static/scripts/assessment/markHandMarking.js"/>'></script>