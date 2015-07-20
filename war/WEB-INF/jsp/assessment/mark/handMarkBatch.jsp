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
	<c:forEach var='user' items="${allUsers}" varStatus="loop">
		<c:set var="name" value="${loop.index < fn:length(myStudents) ? user.username : user.name}" />
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

<h1>${assessmentName} - ${student} -  Submitted: <pasta:readableDate date="${assessmentResult.submissionDate}" /></h1>

<jsp:include page="../../recursive/fileWriterRoot.jsp">
	<jsp:param name="owner" value="${student}"/>
</jsp:include>

<style>
th, td{
	min-width:200px;
}
</style>
<c:choose>
	<c:when test="${not empty student}">
		<form:form commandName="assessmentResult" action="../${nextStudent}/" enctype="multipart/form-data" method="POST">
			<input type="hidden" name="student" value="${student}"/>
			<div class='vertical-block boxCard'>
				<h3 class='compact'>Automatic Marking Results</h3>
				<div class='vertical-block'>
					<h4 class='compact'>Summary</h4>
					<tag:assessmentResult closedAssessment="false" user="${unikey}" results="${assessmentResult}" summary="true" />
				</div>
						
				<div id="${assessmentResult.id}" class='resultDetails vertical-block'>
					<h4 class='compact'><a id='detailsToggle'>Show Details</a></h4>
					<tag:assessmentResult closedAssessment="false" 
						user="${unikey}" results="${assessmentResult}" />
				</div>
			</div>

			<div class='vertical-block boxCard'>
				<h3 class='compact'>Hand Marking Guidelines</h3>
				<c:if test="${empty handMarkingResultList}">
					<p>No hand marking templates for <c:out value="${assessmentResult.groupResult ? 'group' : 'individual'}" /> work submissions.
				</c:if>
				<c:forEach var="handMarkingResult" items="${handMarkingResultList}" varStatus="handMarkingResultStatus">
					<c:set var="weightedHandMarking" value="${handMarkingResult.weightedHandMarking}" />
					<div class='vertical-block' style="width:100%; overflow:auto">
						<h4 class='compact'>${weightedHandMarking.handMarking.name}</h4>
						<c:choose>
							<c:when test="${empty last}">
								<input type="submit" value="Save and continue" id="submit" style="margin-top:1em;"/>
							</c:when>
							<c:otherwise>
								<input type="submit" value="Save and exit" id="submit" style="margin-top:1em;"/>
							</c:otherwise>
						</c:choose>
						<form:input type="hidden" path="handMarkingResults[${handMarkingResultStatus.index}].id" value="${handMarkingResult.id}"/>
						<form:input type="hidden" path="handMarkingResults[${handMarkingResultStatus.index}].weightedHandMarking.id" value="${weightedHandMarking.id}" />
						
						<table id="handMarkingTable${handMarkingResultStatus.index}" style="table-layout:fixed; overflow:auto">
							<thead>
								<tr>
									<th></th> <%-- empty on purpose --%>
									<c:forEach items="${weightedHandMarking.handMarking.columnHeader}" varStatus="columnStatus">
										<th style="cursor:pointer" onclick="clickAllInColumn(this.cellIndex, ${handMarkingResultStatus.index})">
											${weightedHandMarking.handMarking.columnHeader[columnStatus.index].name}<br />
											${weightedHandMarking.handMarking.columnHeader[columnStatus.index].weight}<br />
										</th>
									</c:forEach>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="row" items="${weightedHandMarking.handMarking.rowHeader}" varStatus="rowStatus">
									<tr class='handMarkRow'>
										<th>
											${weightedHandMarking.handMarking.rowHeader[rowStatus.index].name}<br />
											${weightedHandMarking.weight * weightedHandMarking.handMarking.rowHeader[rowStatus.index].weight}
										</th>
										<c:forEach var="column" items="${weightedHandMarking.handMarking.columnHeader}" varStatus="columnStatus">
											<td id="cell_${weightedHandMarking.id}_${weightedHandMarking.handMarking.columnHeader[columnStatus.index].id}_${weightedHandMarking.handMarking.rowHeader[rowStatus.index].id}"><%-- To be filled by javascript --%></td>
										</c:forEach>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</c:forEach>
			</div>
			
			<div class='vertical-block boxCard'>
				<form:textarea style="height:200px; width:95%" path="comments" />
			</div>
			
			<c:choose>
				<c:when test="${empty last}">
					<input type="submit" value="Save and continue" id="submit" style="margin-top:1em;"/>
				</c:when>
				<c:otherwise>
					<input type="submit" value="Save and exit" id="submit" style="margin-top:1em;"/>
				</c:otherwise>
			</c:choose>
			
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
	function fillCells() {
		var cell;
		<c:forEach var="handMarkingResult" items="${handMarkingResultList}" varStatus="handMarkingResultStatus">
			<c:set var="weightedHandMarking" value="${handMarkingResult.weightedHandMarking}" />
			<c:forEach var="datum" items="${weightedHandMarking.handMarking.data}" varStatus="datumStatus">
				cell = document.getElementById("cell_${weightedHandMarking.id}_${datum.column.id}_${datum.row.id}");
				<c:if test="${not empty datum.data or datum.data == \"\"}">
					fillCell(cell, 
						<fmt:formatNumber type='number' maxIntegerDigits='3' value='${weightedHandMarking.weight * datum.row.weight * datum.column.weight}' />,
						"${handMarkingResult.result[datum.row.id] == datum.column.id}",
						"${handMarkingResultStatus.index}",
						"${datum.column.id}",
						"${datum.row.id}",
						"${datum.data}");
				</c:if>
			</c:forEach>
		</c:forEach>
		registerEvents();
	}
	
	$(function() {
		fillCells()
	});
</script>