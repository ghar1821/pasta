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
<jsp:useBean id="now" class="java.util.Date"/>

<h1>
	<c:if test="${not empty viewedUser}">
		${viewedUser.username} -
	</c:if>
	${assessment.name}
</h1>

<h3>Due: ${assessment.dueDate}</h3>
<h3>Worth: ${assessment.marks} marks</h3>

${assessment.description}

<c:choose>
	<c:when test="${not empty history}">
		<h3>History</h3>
		
		<c:forEach var="result" items="${history}" varStatus="resultStatus">
			<h4>${result.submissionDate}</h4>
			<c:if test="${unikey.tutor}">
				<c:set var="node" value="${nodeList[result.formattedSubmissionDate]}" scope="request"/>
				<ul class="list">
					<jsp:include page="../recursive/fileWriter.jsp"/>
				</ul>
			</c:if>
			<div style="width:100%; text-align:right;">
				<button onclick='$("#${resultStatus.index}").slideToggle("slow")'>Details</button>
				<c:if test="${ unikey.tutor }" >
					<!-- tutor abilities -->
					<!-- if assessment has hand marking -->
					<c:if test="${not empty result.assessment.handMarking}">
						<!-- edit marking if already marked -->
						<c:choose>
							<c:when test="${not empty viewedUser}">
								<c:choose>
									<c:when test="${ not result.finishedHandMarking }">
										<button onclick="window.location.href='../../../../mark/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Mark attempt</button>
									</c:when>
									<c:otherwise>
										<button onclick="window.location.href='../../../../mark/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Edit attempt marks</button>
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<c:choose>
									<c:when test="${ not result.finishedHandMarking }">
										<button onclick="window.location.href='../../mark/${unikey.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Mark attempt</button>
									</c:when>
									<c:otherwise>
										<button onclick="window.location.href='../../mark/${unikey.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Edit attempt marks</button>
									</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>
					</c:if>
					<!-- if the assessment contains unit tests -->
					<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
						<c:choose>
							<c:when test="${not empty viewedUser}">	
								<button onclick="window.location.href='../../../../runAssessment/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
							</c:when>
							<c:otherwise>
								<button onclick="window.location.href='../../runAssessment/${unikey.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
							</c:otherwise>
						</c:choose>
					</c:if>
					<c:choose>
						<c:when test="${not empty viewedUser}">	
							<button onclick="window.location.href='../../../../download/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Download attempt</button>
						</c:when>
						<c:otherwise>
							<button onclick="window.location.href='../../download/${viewedUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Download attempt</button>
						</c:otherwise>
					</c:choose>
				</c:if>
			</div>
			<c:if test="${ unikey.tutor }" >
				<h5>Files Compiled <a onclick='$("#files_${resultStatus.index}").slideToggle("fast")'>(toggle)</a></h5>
				<div id="files_${resultStatus.index}" class="ui-state-highlight ui-corner-all" style="font-size: 1em;display:none;padding:1em;">
					<c:forEach var="unitTest" items="${result.unitTests}">
						<h6 style="margin-top:0.8em;">${unitTest.test.name}</h6>
						<pre><c:out value="${unitTest.filesCompiled}" escapeXml="true"/></pre>
					</c:forEach>
				</div>
			</c:if>
			<c:choose>
				<c:when test="${result.compileError}">
					<h5>Compile Errors</h5>
					<div id="${resultStatus.index}" class="ui-state-error ui-corner-all" style="font-size: 1em;display:none;padding:1em;">
						<c:forEach var="compError" items="${result.compilationErrors}">
							<div style="margin-top:1em;"><pre><c:out value="${compError}" escapeXml="true"/></pre></div>
						</c:forEach>
					</div>
				</c:when>
				<c:otherwise>
					<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
						<c:forEach var="allUnitTests" items="${result.unitTests}">
							<c:choose>
								<c:when test="${allUnitTests.secret}">
									<c:choose>
										<c:when test="${unikey.tutor or ((assessment.dueDate lt now) and (empty viewedUser.extensions[assessment.id] or viewedUser.extensions[assessment.id] lt now))}">
											<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
												<div class="pastaUnitTestBoxResult secret ${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp;</div>
											</c:forEach>
										</c:when>
										<c:otherwise>
											<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
												<div class="pastaUnitTestBoxResult secret" title="???">&nbsp;</div>
											</c:forEach>
										</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
									<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
										<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp;</div>
									</c:forEach>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</c:if>
					<div id="${resultStatus.index}" style="display:none; clear:left;">
						<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
							<table class="pastaTable" >
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
						<c:if test="${not empty viewedUser}">
							<button type="button" onclick="$('div#updateComments${assessment.id}').show();$(this).hide()">Modify Comments</button>
							<div id="updateComments${assessment.id}"  style="display:none;">
								<form action="updateComment/" enctype="multipart/form-data" method="POST">
									<input name="resultId" type="hidden" value="${result.id}">
									<%--<input name="assessmentDate" type="hidden" value="${result.formattedSubmissionDate}"> --%>
									<textarea name="newComment" cols="110" rows="10" id="modifyComments${assessment.id}" ><c:choose><c:when test="${empty result.comments}">No comments</c:when><c:otherwise>${result.comments}</c:otherwise></c:choose></textarea>
									<button id="updateComments${assessment.id}" type="submit" >Update Comments</button>
								</form>
								<script>
							    $(function() {
									$("#modifyComments${assessment.id}").on('keyup', function() {
							            $("#comments${assessment.id}").html(document.getElementById("modifyComments${assessment.id}").value);
							        });
							    });
								
								(function($) {
									$(document).ready(function() {
										$('#modifyComments${assessment.id}').wysiwyg({
											initialContent: function() {
												return value_of_textarea;
											},
										  controls: {
											bold          : { visible : true },
											italic        : { visible : true },
											underline     : { visible : true },
											strikeThrough : { visible : true },
											
											justifyLeft   : { visible : true },
											justifyCenter : { visible : true },
											justifyRight  : { visible : true },
											justifyFull   : { visible : true },

											indent  : { visible : true },
											outdent : { visible : true },

											subscript   : { visible : true },
											superscript : { visible : true },
											
											undo : { visible : true },
											redo : { visible : true },
											
											insertOrderedList    : { visible : true },
											insertUnorderedList  : { visible : true },
											insertHorizontalRule : { visible : true },

											h4: {
												visible: true,
												className: 'h4',
												command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
												arguments: ($.browser.msie || $.browser.safari) ? '<h4>' : 'h4',
												tags: ['h4'],
												tooltip: 'Header 4'
											},
											h5: {
												visible: true,
												className: 'h5',
												command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
												arguments: ($.browser.msie || $.browser.safari) ? '<h5>' : 'h5',
												tags: ['h5'],
												tooltip: 'Header 5'
											},
											h6: {
												visible: true,
												className: 'h6',
												command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
												arguments: ($.browser.msie || $.browser.safari) ? '<h6>' : 'h6',
												tags: ['h6'],
												tooltip: 'Header 6'
											},
											cut   : { visible : true },
											copy  : { visible : true },
											paste : { visible : true },
											html  : { visible: true },
											increaseFontSize : { visible : true },
											decreaseFontSize : { visible : true },
											exam_html: {
												exec: function() {
													this.insertHtml('<abbr title="exam">Jam</abbr>');
													return true;
												},
												visible: true
											}
										  }
										});
									});
								})(jQuery);
								</script>
							</div>
						</c:if>
					</div>
				</c:otherwise>
			</c:choose>
			<c:if test="${not empty result.assessment.handMarking and result.finishedHandMarking}">
				<h5>Hand Marking</h5>
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
			</c:if>
			<h5>Comments</h5>
			<div id="comments${assessment.id}">
				<c:choose>
					<c:when test="${empty result.comments}">
						No comments
					</c:when>
					<c:otherwise>
						${result.comments}
					</c:otherwise>
				</c:choose>
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
