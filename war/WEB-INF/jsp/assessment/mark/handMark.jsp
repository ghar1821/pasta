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