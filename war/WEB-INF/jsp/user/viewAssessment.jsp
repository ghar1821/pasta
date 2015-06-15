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

<jsp:useBean id="now" class="java.util.Date"/>

<h1>
	<c:if test="${not empty viewedUser}">
		${viewedUser.username} -
	</c:if>
	${assessment.name}
</h1>

<table>
	<tr><th>Due Date</th><td><pasta:readableDate date="${assessment.dueDate}" /></td></tr>
	<tr><th>Possible Marks</th><td>${assessment.marks}</td></tr>
</table>

<c:if test="${not empty assessment.description}">
	<h6>Description</h6>
	${assessment.description}
</c:if>

<c:choose>
	<c:when test="${not empty history}">
		<h2>Submission History</h2>
		
		<c:forEach var="result" items="${history}" varStatus="resultStatus">
		<div class='vertical-block boxCard'>
			<%--Heading and button panel--%>
			<div class='vertical-block float-container'>
				<div class='float-left'>
					<h4 class='compact showHide' showhide='${result.id}'><pasta:readableDate date="${result.submissionDate}" /></h4>
				</div>
				<c:if test="${ unikey.tutor }" >
					<div id='buttonPanel' class='float-right'>
						<c:set var="pathPrefix" value="../.." />
						<c:if test="${not empty viewedUser}">
							<c:set var="pathPrefix" value="../../../.." />
						</c:if>
						<c:if test="${not empty result.assessment.handMarking}">
							<c:set var="markButtonText" value="Mark attempt" />
							<c:if test="${result.finishedHandMarking}">
								<c:set var="markButtonText" value="Edit attempt marks" />
							</c:if>
							<button onclick="window.location.href='${pathPrefix}/mark/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >${markButtonText}</button>
						</c:if>
						<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
							<button onclick="window.location.href='${pathPrefix}/runAssessment/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
						</c:if>
						<button onclick="window.location.href='${pathPrefix}/download/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Download attempt</button>
					</div>
				</c:if>
			</div>
			
			<%--Submission contents--%>
			<c:if test="${unikey.tutor}">
				<div class='vertical-block'>
					<c:set var="node" value="${nodeList[result.formattedSubmissionDate]}" scope="request"/>
					<ul class="list">
						<jsp:include page="../recursive/fileWriter.jsp"/>
					</ul>
				</div>
			</c:if>
			
			<%--Summary of results--%>
			<div class='vertical-block float-container'>
				<c:choose>
					<c:when test="${result.compileError}">
						<h4 class='compact'>Compile Errors</h4>
					</c:when>
					<c:when test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
						<div class='float-container'>
							<c:forEach var="allUnitTests" items="${result.unitTests}">
								<c:set var="secret" value="${allUnitTests.secret}"/>
								<c:set var="revealed" value="${secret and (unikey.tutor or ((assessment.dueDate lt now) and (empty viewedUser.extensions[assessment.id] or viewedUser.extensions[assessment.id] lt now)))}" />
								<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
									<div class="unitTestResult <c:if test="${not secret or revealed}">${unitTestCase.testResult}</c:if>
									<c:if test="${revealed}">revealed</c:if> <c:if test="${secret}">secret</c:if>" 
									title="<c:choose><c:when test="${revealed or not secret}">${unitTestCase.testName}</c:when><c:otherwise>???</c:otherwise></c:choose>">&nbsp;</div>
								</c:forEach>
							</c:forEach>
						</div>
					</c:when>
					<c:otherwise>
						<h3 class='compact'>Result Error</h3>
					</c:otherwise>
				</c:choose>
			</div>
			
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
			
			<%--Details of results--%>
			<div id="${result.id}" class='resultDetails vertical-block'>
				<c:choose>
					<c:when test="${result.compileError}">
						<div class="ui-state-error ui-corner-all" style="font-size: 1em;padding:1em;">
							<c:forEach var="compError" items="${result.compilationErrors}">
								<div style="margin-top:1em;"><pre><c:out value="${compError}" escapeXml="true"/></pre></div>
							</c:forEach>
						</div>
					</c:when>
					<c:otherwise>
						<div class='vertical-block'>
							<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
								<table class="pastaTable" style='margin:0px'>
									<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
									<c:forEach var="allUnitTests" items="${result.unitTests}">
										<c:forEach var="testCase" items="${allUnitTests.testCases}">
											<tr>
												<c:choose>
													<c:when test="${allUnitTests.secret}">
														<c:choose>
															<c:when test="${unikey.tutor or ((assessment.dueDate lt now) and (empty unikey.extensions[assessment.id] or unikey.extensions[assessment.id] lt now))}">
																<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
																<td style="text-align:left;">Secret - ${testCase.testName}</td>
																<td>${testCase.time}</td>
																<td>
																	<pre>${testCase.type} - <c:out value="${testCase.testMessage}" escapeXml="true"/></pre>
																</td>
															</c:when>
															<c:otherwise>
																<td><span class="pastaUnitTestResult pastaUnitTestResultSecret">hidden</span></td>
																<td style="text-align:left;">???</td>
																<td>???</td>
																<td>???</td>
															</c:otherwise>
														</c:choose>
													</c:when>
													<c:otherwise>
														<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
														<td style="text-align:left;">${testCase.testName}</td>
														<td>${testCase.time}</td>
														<td>
															<pre>${testCase.type} - <c:out value="${testCase.testMessage}" escapeXml="true"/></pre>
														</td>
													</c:otherwise>
												</c:choose>
											</tr>
										</c:forEach>
									</c:forEach>
								</table>
							</c:if>
						</div>
					</c:otherwise>
				</c:choose>
				<c:if test="${not empty result.assessment.handMarking and result.finishedHandMarking}">
					<div class='vertical-block'>
						<h5 class='compact'>Hand Marking</h5>
						<c:forEach var="handMarking" items="${result.assessment.handMarking}" varStatus="handMarkingStatus">
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
							<button type="button" onclick="$('div#updateComments${result.id}').show();$(this).hide()">Modify Comments</button>
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
					<c:if test="${not empty result.assessment.handMarking and result.finishedHandMarking}">
						<c:forEach var="handMarking" items="${result.assessment.handMarking}" varStatus="handMarkingStatus">
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
