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

<c:set var="nextStudent" value="-1"/>
<c:forEach var="student" items="${hasSubmission}" varStatus="studentStatus">
		<c:choose>
			<c:when test="${student && savingStudentIndex == studentStatus.index}">
				<a href="../${studentStatus.index}/" ><div title="${myStudents[studentStatus.index].username}" class="handMarkingStudent submitted${completedMarking[studentStatus.index]} current">&nbsp;</div></a>
			</c:when>
			<c:when test="${student}">
				<a href="../${studentStatus.index}/" ><div title="${myStudents[studentStatus.index].username}" class="handMarkingStudent submitted${completedMarking[studentStatus.index]}">&nbsp;</div></a>
				<c:if test="${savingStudentIndex < studentStatus.index && nextStudent == -1}">
					<c:set var="nextStudent" value="${studentStatus.index}"/>
				</c:if>
			</c:when>
			<c:otherwise>
				<div title="${myStudents[studentStatus.index].username}" class="handMarkingStudent didnotsubmit">&nbsp;</div>
			</c:otherwise>
		</c:choose>
</c:forEach>

<h1>${assessmentName} - ${student} -  Submitted: ${assessmentResult.submissionDate}</h1>

<ul class="list">
<jsp:include page="../../recursive/fileWriter.jsp"/>
</ul>

<style>
th, td{
	min-width:200px;
}
</style>
<c:choose>
	<c:when test="${not empty student}">
		<form:form commandName="assessmentResult" action="../${nextStudent}/" enctype="multipart/form-data" method="POST">
		<input type="hidden" name="student" value="${student}"/>
		<c:choose>
				<c:when test="${assessmentResult.compileError}">
					<div style="width:100%; text-align:right;">
						<button type="button" onclick='$("#details").slideToggle("slow")'>Details</button>
					</div>
					<h5>Compile Errors</h5>
					<div id="details" class="ui-state-error ui-corner-all" style="font-size: 1em;display:none;">
						<pre>
							${assessmentResult.compilationError}
						</pre>
					</div>
				</c:when>
				<c:otherwise>
					<h3>Automatic Marking Results</h3>
					<c:if test="${not empty assessmentResult.assessment.unitTests or not empty assessmentResult.assessment.secretUnitTests}">
						<div class='float-container'>
							<c:forEach var="allUnitTests" items="${assessmentResult.unitTests}">
								<c:set var="secret" value="${allUnitTests.secret}"/>
								<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
									<div class="unitTestResult ${unitTestCase.testResult} <c:if test="${secret}">revealed</c:if>"
									 title="${unitTestCase.testName}">&nbsp;</div>
								</c:forEach>
							</c:forEach>
						</div>
					</c:if>
					<div style="width:100%; text-align:right;">
						<button type=button onclick='$("#details").slideToggle("slow")'>Details</button>
					</div>
					<div id="details" style="display:none">
						<c:if test="${not empty assessmentResult.assessment.unitTests or not empty assessmentResult.assessment.secretUnitTests}">
							<table class="pastaTable" >
								<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
								<c:forEach var="allUnitTests" items="${assessmentResult.unitTests}">
									<c:forEach var="testCase" items="${allUnitTests.testCases}">
										<tr>
											<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
											<td style="text-align:left;">${testCase.testName}</td>
											<td>${testCase.time}</td>
											<td>
												<pre>${testCase.type} - ${fn:replace(fn:replace(testCase.testMessage, '>', '&gt;'), '<', '&lt;')}</pre>

											</td>
										</tr>
									</c:forEach>
								</c:forEach>
							</table>
						</c:if>
					</div>
				</c:otherwise>
			</c:choose>

			<h3>Hand Marking Guidelines</h3>
			<c:set var="totalHandMarkingCategories" value="0" />
			<c:forEach var="handMarkingResult" items="${handMarkingResultList}" varStatus="handMarkingResultStatus">
				<c:set var="weightedHandMarking" value="${handMarkingResult.weightedHandMarking}" />
				<h4>${weightedHandMarking.handMarking.name}</h4>
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
				<div style="width:100%; overflow:auto">
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
								<c:set var="totalHandMarkingCategories" value="${totalHandMarkingCategories + 1}" />
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
			
			<form:textarea style="height:200px; width:95%" path="comments" />
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