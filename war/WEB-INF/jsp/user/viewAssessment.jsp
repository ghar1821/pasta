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

<%@ taglib prefix="pasta" uri="pastaTag"%>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>


<jsp:useBean id="now" class="java.util.Date"/>

<c:set var="user" value="${not empty viewedUser ? viewedUser : unikey}" />

<h1>
	${user.username} - ${assessment.name}
</h1>

<table class='alignCellsTop'>
	<tr><th>Due Date</th><td><pasta:readableDate date="${assessment.dueDate}" /></td></tr>
	<tr><th>Possible Marks</th><td>${assessment.marks}</td></tr>
	<c:if test="${not empty assessment.submissionLanguages}">
		<tr><th>Allowed Languages</th><td>
			<c:forEach var="language" items="${assessment.submissionLanguages}">
				<div><code>${language.description}</code></div>
			</c:forEach>
		</td></tr>
	</c:if>
</table>

<c:if test="${not empty assessment.description}">
	<h4>Assessment Description</h4>
	${assessment.description}
</c:if>

<c:if test="${assessment.autoMarked}">
	<h4>General Submission Instructions</h4>
	<c:if test="${not empty assessment.solutionName}">
	<c:choose>
		<c:when test="${empty assessment.submissionLanguages}">
			<p>Your submission must be in a file named <code>${assessment.solutionName}</code>.
		</c:when>
		<c:otherwise>
			<p>Your submission must be written in <c:choose><c:when test="${fn:length(assessment.submissionLanguages) == 1}">the language</c:when><c:otherwise>one of the languages</c:otherwise></c:choose> listed above. It must include a main file named <code>${assessment.solutionName}</code> (for example <code>${assessment.sampleSubmissionName}</code>).
		</c:otherwise>
	</c:choose>
	</c:if>
	<c:choose>
		<c:when test="${fn:length(assessment.expectedDirectories) == 1}">
			<p>Your code submission must be in a directory called <code>${assessment.expectedDirectories[0]}</code>.
		</c:when>
		<c:when test="${not empty assessment.expectedDirectories}">
			<p>Your code submission must contain the following code directories:
			<c:forEach var="dir" items="${assessment.expectedDirectories}" varStatus="loop">
				<code>${dir}</code><c:if test="${!loop.last}">, </c:if>
			</c:forEach>
		</c:when>
	</c:choose>
	<p><c:if test="${empty assessment.expectedDirectories}">If your submission contains multiple files, </c:if><strong>zip</strong> your submission into a <code>.zip</code> file and submit that. Do not use any other zip format (e.g. <code>.rar</code> or <code>.7z</code>).
</c:if>

<c:choose>
	<c:when test="${not empty history}">
		<h2>Submission History</h2>
		
		<c:forEach var="result" items="${history}" varStatus="resultStatus">
		<c:set var='groupResult' value="${result.user.id != user.id}" />
		<div class='vertical-block boxCard <c:if test="${groupResult}">group-highlight</c:if>'>
			<%--Heading and button panel--%>
			<div class='vertical-block float-container'>
				<div class='float-left'>
					<h4 class='compact showHide' showhide='${result.id}'><pasta:readableDate date="${result.submissionDate}" /></h4>
				</div>
				<c:if test="${ unikey.tutor }" >
					<div id='buttonPanel' class='float-right horizontal-block'>
						<c:set var="pathPrefix" value="../.." />
						<c:if test="${not empty viewedUser}">
							<c:set var="pathPrefix" value="../../../.." />
						</c:if>
						<c:if test="${not empty result.assessment.handMarking}">
							<c:set var="markButtonText" value="Mark attempt" />
							<c:if test="${result.finishedHandMarking}">
								<c:set var="markButtonText" value="Edit attempt marks" />
							</c:if>
							<button onclick="window.location.href='${pathPrefix}/mark/${user.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >${markButtonText}</button>
						</c:if>
						<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
							<button onclick="window.location.href='${pathPrefix}/runAssessment/${user.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
						</c:if>
						<button onclick="window.location.href='${pathPrefix}/download/${user.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Download attempt</button>
					</div>
				</c:if>
				<c:if test="${groupResult}">
					<div class='submitted-by-panel float-right'>
						<div class='align-contents-middle horizontal-block'>
							<div class='horizontal-block small-gap'><h4 class='alt compact'>Group Submission</h4></div>
							<div class='horizontal-block small-gap icon-group'></div>
							<div class='horizontal-block small-gap'><p>Submitted by ${result.submittedBy}.</div>
						</div>
					</div>
				</c:if>
			</div>
			
			<%--Submission contents--%>
			<c:if test="${unikey.tutor}">
				<div class='vertical-block'>
					<c:set var="node" value="${nodeList[result.formattedSubmissionDate]}" scope="request"/>
					<jsp:include page="../recursive/fileWriterRoot.jsp"/>
				</div>
			</c:if>
			
			<%--Summary of results--%>
			<h5 class='compact'>Summary</h5>
			<c:set var="closed" value="${((assessment.dueDate lt now) and (empty viewedUser.extensions[assessment.id] or viewedUser.extensions[assessment.id] lt now))}" />
			<tag:assessmentResult closedAssessment="${closed}" user="${user}" results="${result}" summary="true" />
			
			<%--Details of results--%>
			<div id="${result.id}" class='resultDetails vertical-block'>
				<h5 class='compact'>Details</h5>
				<%--Files compiled--%>
				<c:if test="${ unikey.tutor }" >
					<div class='vertical-block'>
						<p class='showHide' showhide="files_${resultStatus.index}"><strong>Files Compiled</strong>
						<div id="files_${resultStatus.index}" class="ui-state-highlight ui-corner-all" style="font-size: 1em;display:none;padding:1em;">
							<c:forEach var="unitTest" items="${result.unitTests}">
								<div class='vertical-block'>
									<h6 class='compact'>${unitTest.test.name}</h6>
									<pre><c:out value="${unitTest.filesCompiled}" escapeXml="true"/></pre>
								</div>
							</c:forEach>
						</div>
					</div>
				</c:if>
				
				<tag:assessmentResult closedAssessment="${closed}" 
					user="${user}" results="${result}" />

				<c:set var="relevantHandMarking" value="${groupResult ? result.assessment.groupHandMarking : result.assessment.individualHandMarking}" />
				<c:if test="${not empty relevantHandMarking and result.finishedHandMarking}">
					<div class='vertical-block'>
						<h5 class='compact'>Hand Marking</h5>
						<c:forEach var="handMarking" items="${relevantHandMarking}" varStatus="handMarkingStatus">
							<div style="width:100%; overflow:auto">
								<table id="handMarkingTable${handMarkingStatus.index}" style="table-layout:fixed; overflow:auto">
									<thead>
										<tr>
											<th></th> <!-- empty on purpose -->
											<c:forEach items="${handMarking.handMarking.columnHeader}" varStatus="columnStatus">
												<th style="cursor:pointer">
													${handMarking.handMarking.columnHeader[columnStatus.index].name}<br />
													${handMarking.handMarking.columnHeader[columnStatus.index].weight*100}%<br />
												</th>
											</c:forEach>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="row" items="${handMarking.handMarking.rowHeader}" varStatus="rowStatus">
											<tr>
												<th>
													${handMarking.handMarking.rowHeader[rowStatus.index].name}<br />
													<fmt:formatNumber type="number" maxIntegerDigits="3" value="${handMarking.handMarking.rowHeader[rowStatus.index].weight*handMarking.weight}" />
												</th>
												<c:forEach var="column" items="${handMarking.handMarking.columnHeader}">
													<td id="cell_${result.id}_${handMarking.id}_${column.id}_${row.id}" <c:if test="${result.handMarkingResults[handMarkingStatus.index].result[row.id] == column.id}" > class="selectedMark" </c:if>><%-- To be filled with JavaScript --%></td>
												</c:forEach>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</c:forEach>
					</div>
				</c:if>
				<div class='vertical-block'>
					<h5 class='compact'>Comments</h5>
					<div id="comments${result.id}" class='vertical-block'>
						<c:choose>
							<c:when test="${empty result.comments}">
								No comments
							</c:when>
							<c:otherwise>
								${result.comments}
							</c:otherwise>
						</c:choose>
					</div>
					<c:if test="${not empty viewedUser}">
						<div class='vertical-block'>
							<div id="updateComments${result.id}" style="display:none;">
								<form action="updateComment/" enctype="multipart/form-data" method="POST">
									<input name="resultId" type="hidden" value="${result.id}">
									<textarea name="newComment" cols="110" rows="10" id="modifyComments${result.id}" ><c:choose><c:when test="${empty result.comments}">No comments</c:when><c:otherwise>${result.comments}</c:otherwise></c:choose></textarea>
									<button type="submit" >Save Comments</button>
								</form>
							</div>
							<button type="button" class='modifyCommentsBtn'>Modify Comments</button>
						</div>
					</c:if>
				</div>
			</div>
		</div>
		</c:forEach>
		<script src='<c:url value="/static/scripts/assessment/userViewAssessment.js"/>'></script>
		<script>		
			function fillCells() {
				var cell;
				<c:forEach var="result" items="${history}" varStatus="resultStatus">
					<c:if test="${not empty relevantHandMarking and result.finishedHandMarking}">
						<c:forEach var="handMarking" items="${relevantHandMarking}" varStatus="handMarkingStatus">
							<c:forEach var="datum" items="${handMarking.handMarking.data}" varStatus="datumStatus">
								cell = document.getElementById("cell_${result.id}_${handMarking.id}_${datum.column.id}_${datum.row.id}");
								<c:if test="${not empty datum.data or datum.data == \"\"}">
									fillCell(cell, 
										<fmt:formatNumber type='number' maxIntegerDigits='3' value='${handMarking.weight * datum.row.weight * datum.column.weight}' />,
										"${datum.data}");
								</c:if>
							</c:forEach>
						</c:forEach>
					</c:if>
				</c:forEach>
			}
			
			$(function() {
				fillCells()
			});
		</script>
	</c:when>
	
	<c:otherwise>
		<h3>Assessment not attempted</h3>
	</c:otherwise>
</c:choose>
