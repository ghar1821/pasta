<%--
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
--%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>

<%@ taglib prefix="pasta" uri="pastaTag"%>

<c:set var="nextStudent" value="-1"/>

<div class='section'>
	<div class='part float-container'>
		<c:if test="${not empty myStudents}">
			<h3 class='part-title'>Individual Submissions</h3>
		</c:if>
		<c:forEach var='submittingUser' items="${allUsers}" varStatus="loop">
			<c:set var="submission" value="${hasSubmission[loop.index]}" />
			<c:set var="completed" value="${completedMarking[loop.index]}" />
			<c:set var="current" value="${loop.index == savingStudentIndex}" />
			<c:set var="divClass" value="handMarkingStudent ${submission ? 'submitted' : 'didnotsubmit'}${submission ? completed : ''} ${current ? 'current' : ''}" />
			<c:if test="${loop.index == fn:length(myStudents)}">
				</div>
				<div class='part float-container'>
					<h3 class='part-title'>Group Submissions</h3>
			</c:if>
			<c:if test="${submission}">
				<c:if test="${savingStudentIndex < loop.index && nextStudent == -1}">
					<c:set var="nextStudent" value="${loop.index}"/>
				</c:if>
				<a href="../${loop.index}/" >
			</c:if>
			<div data-hover="hover${loop.index}" class="hover ${divClass}">&nbsp;</div>
			<div class="hover${loop.index} hidden">
				<c:choose>
					<c:when test="${loop.index < fn:length(myStudents)}">
						${submittingUser.username}
					</c:when>
					<c:otherwise>
						${submittingUser.name}:
						<ul>
							<c:forEach items="${submittingUser.members}" var="member">
								<li>${member.username} - ${member.tutorial}
							</c:forEach>
						</ul>
					</c:otherwise>
				</c:choose>
			</div>
			<c:if test="${submission}">
				</a>
			</c:if>
		</c:forEach>
	</div>
</div>




<c:set var="username" value="${student.group ? student.name : student.username}" />
<h1><c:if test="${not student.group}">${assessmentName} - </c:if>${username}</h1>

<div class='section'>
	<c:if test="${student.group}">
		<div class='part no-line'>
			<h3 class='part-title'>Group Members:</h3>
			<ul>
			<c:forEach var="member" items="${studentsInGroup}">
				<li><a href='../../../student/${member.username}/home/'>${member.username}</a>
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
			<jsp:param name="owner" value="${username}"/>
		</jsp:include>
	</div>
</div>

<style>
th, td{
	min-width:200px;
}
</style>
<c:choose>
	<c:when test="${not empty student}">
		<form:form commandName="assessmentResult" action="../${nextStudent}/" enctype="multipart/form-data" method="POST">
			<input type="hidden" name="student" value="${student.username}"/>
			<div class='section'>
				<h3 class='section-title'>Automatic Marking Results</h3>
				<div class='part'>
					<h4 class='part-title'>Summary</h4>
					<tag:unitTestResult closedAssessment="false" results="${assessmentResult}" summary="true" />
				</div>
				<div class='part'>
					<h4 class='part-title'><a id='detailsToggle'>Show Details</a></h4>
					<div id="${assessmentResult.id}" class='resultDetails'>
						<tag:unitTestResult closedAssessment="false" 
							results="${assessmentResult}" />
					</div>
				</div>
			</div>

			<div class='section'>
				<tag:handMarkingResult results="${assessmentResult}" marking="true" heading="Hand Marking Guidelines"/>
			</div>
			
			<div class='section'>
				<div class='part no-line'>
					<h3 class='part-title'>Comments</h3>
					<form:textarea style="height:200px;" path="comments" />
				</div>
				<div class='button-panel'>
					<button type="submit" class='save_hand_marking' id="submit">Save and continue</button>
				</div>
			</div>
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
	$(function() {registerEvents
		$(".save_hand_marking").text('${empty last ? "Save and continue" : "Save and exit"}');
	
		$(".hover").tipsy({
			gravity: 'n',			
			html: true,
			title: function() {
				return $("." + $(this).data("hover")).html();
			}
		});
	});
</script>
