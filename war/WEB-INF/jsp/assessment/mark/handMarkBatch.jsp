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

<c:set var="nextStudent" value="-1"/>
<div class='vertical-block float-container'>
	<c:if test="${not empty myStudents}">
		<h3 class='compact'>Individual Submissions</h3>
	</c:if>
	<c:forEach var='submittingUser' items="${allUsers}" varStatus="loop">
		<c:set var="name" value="${loop.index < fn:length(myStudents) ? submittingUser.username : submittingUser.name}" />
		<c:set var="submission" value="${hasSubmission[loop.index]}" />
		<c:set var="completed" value="${completedMarking[loop.index]}" />
		<c:set var="current" value="${loop.index == savingStudentIndex}" />
		<c:set var="divClass" value="handMarkingStudent ${submission ? 'submitted' : 'didnotsubmit'}${submission ? completed : ''} ${current ? 'current' : ''}" />
		<c:if test="${loop.index == fn:length(myStudents)}">
			</div>
			<div class='vertical-block float-container'>
				<h3 class='compact'>Group Submissions</h3>
		</c:if>
		<c:if test="${submission}">
			<c:if test="${savingStudentIndex < loop.index && nextStudent == -1}">
				<c:set var="nextStudent" value="${loop.index}"/>
			</c:if>
			<a href="../${loop.index}/" >
		</c:if>
		<div title="${name}" class="${divClass}">&nbsp;</div>
		<c:if test="${submission}">
			</a>
		</c:if>
	</c:forEach>
</div>

<c:set var="username" value="${student.group ? student.name : student.username}" />
<h1><c:if test="${not student.group}">${assessmentName} - </c:if>${username} - Submitted: <pasta:readableDate date="${assessmentResult.submissionDate}" /></h1>

<jsp:include page="../../recursive/fileWriterRoot.jsp">
	<jsp:param name="owner" value="${username}"/>
</jsp:include>

<style>
th, td{
	min-width:200px;
}
</style>
<c:choose>
	<c:when test="${not empty student}">
		<form:form commandName="assessmentResult" action="../${nextStudent}/" enctype="multipart/form-data" method="POST">
			<input type="hidden" name="student" value="${student.username}"/>
			<div class='vertical-block boxCard'>
				<h3 class='compact'>Automatic Marking Results</h3>
				<div class='vertical-block'>
					<h4 class='compact'>Summary</h4>
					<tag:unitTestResult closedAssessment="false" results="${assessmentResult}" summary="true" />
				</div>
						
				<div id="${assessmentResult.id}" class='resultDetails vertical-block'>
					<h4 class='compact'><a id='detailsToggle'>Show Details</a></h4>
					<tag:unitTestResult closedAssessment="false" 
						results="${assessmentResult}" />
				</div>
			</div>

			<div class='vertical-block boxCard'>
				<tag:handMarkingResult results="${assessmentResult}" marking="true" heading="Hand Marking Guidelines"/>
			</div>
			
			<div class='vertical-block boxCard'>
				<form:textarea style="height:200px; width:95%" path="comments" />
			</div>
			
			<input type="submit" class='save_hand_marking' value="Save and continue" id="submit" style="margin-top:1em;"/>
		</form:form>
	</c:when>
	<c:otherwise>
		<b>No submissions left to mark</b>
	</c:otherwise>
</c:choose>

<div class="popup" id="confirmPopup">
	<span class="button bClose"> <span><b>X</b></span>
	</span>
	<h1>Would you like to save your changes?</h1>
	<button id="yesButton" onclick="">Yes</button>
	<button id="noButton" onclick="">No</button>
</div>

<script src='<c:url value="/static/scripts/assessment/markHandMarking.js"/>'></script>
<script>
	$(function() {
		$(".save_hand_marking").val('${empty last ? "Save and continue" : "Save and exit"}');
	});
</script>
