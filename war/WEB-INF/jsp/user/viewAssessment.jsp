<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
			<c:choose>
				<c:when test="${result.compileError}">
					<div style="width:100%; text-align:right;">
						<button onclick='$("#${resultStatus.index}").slideToggle("slow")'>Details</button>
					</div>
					<h5>Compile Errors</h5>
					<div id="${resultStatus.index}" class="ui-state-error ui-corner-all" style="font-size: 1em;display:none;">
						<pre>
							${result.compilationError}
						</pre>
					</div>
				</c:when>
				<c:otherwise>
					<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
						<c:forEach var="allUnitTests" items="${result.unitTests}">
							<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
								<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
							</c:forEach>
						</c:forEach>
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
												<button onClick="window.location.href='../../../../mark/${viewedUser.username}/${result.assessment.shortName}/${result.formattedSubmissionDate}/'" >Mark attempt</button>
											</c:when>
											<c:otherwise>
												<button onClick="window.location.href='../../../../mark/${viewedUser.username}/${result.assessment.shortName}/${result.formattedSubmissionDate}/'" >Edit attempt marks</button>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:choose>
											<c:when test="${ not result.finishedHandMarking }">
												<button onClick="window.location.href='../../mark/${unikey.username}/${result.assessment.shortName}/${result.formattedSubmissionDate}/'" >Mark attempt</button>
											</c:when>
											<c:otherwise>
												<button onClick="window.location.href='../../mark/${unikey.username}/${result.assessment.shortName}/${result.formattedSubmissionDate}/'" >Edit attempt marks</button>
											</c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</c:if>
							<!-- if the assessment contains unit tests -->
							<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
								<c:choose>
									<c:when test="${not empty viewedUser}">	
										<button onClick="window.location.href='../../../../runAssessment/${viewedUser.username}/${result.assessment.shortName}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
									</c:when>
									<c:otherwise>
										<button onClick="window.location.href='../../runAssessment/${unikey.username}/${result.assessment.shortName}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
									</c:otherwise>
								</c:choose>
							</c:if>
						</c:if>
					</div>
					<div id="${resultStatus.index}" style="display:none">
						<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
							<table class="pastaTable" >
								<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
								<c:forEach var="allUnitTests" items="${result.unitTests}">
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
						<c:if test="${not empty viewedUser}">
							<button type="button" onclick="$('div#updateComments${assessment.shortName}').show();$(this).hide()">Modify Comments</button>
							<div id="updateComments${assessment.shortName}"  style="display:none;">
								<form action="updateComment/" enctype="multipart/form-data" method="POST">
									<input name="assessmentDate" type="hidden" value="${result.formattedSubmissionDate}">
									<textarea name="newComment" cols="110" rows="10" id="modifyComments${assessment.shortName}" ><c:choose><c:when test="${empty result.comments}">No comments</c:when><c:otherwise>${result.comments}</c:otherwise></c:choose></textarea>
									<button id="updateComments${assessment.shortName}" type="submit" >Update Comments</button>
								</form>
								<script>
							    $(function() {
									$("#modifyComments${assessment.shortName}").on('keyup', function() {
							            $("#comments${assessment.shortName}").html(document.getElementById("modifyComments${assessment.shortName}").value);
							        });
							    });
								</script>
							</div>
						</c:if>
					</div>
				</c:otherwise>
			</c:choose>
			<c:if test="${not empty result.assessment.handMarking and result.finishedHandMarking}">
				<h5>Hand Marking</h2>
				<c:forEach var="handMarking" items="${result.assessment.handMarking}" varStatus="handMarkingStatus">
					<div style="width:100%; overflow:auto">
						<table id="handMarkingTable${handMarkingStatus.index}" style="table-layout:fixed; overflow:auto">
							<thead>
								<tr>
									<th></th> <!-- empty on purpose -->
									<c:forEach items="${handMarking.handMarking.columnHeader}" varStatus="columnStatus">
										<th style="cursor:pointer">
											${handMarking.handMarking.columnHeader[columnStatus.index].name}</br>
											${handMarking.handMarking.columnHeader[columnStatus.index].weight}</br>
										</th>
									</c:forEach>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="row" items="${handMarking.handMarking.rowHeader}" varStatus="rowStatus">
									<tr>
										<th>
											${handMarking.handMarking.rowHeader[rowStatus.index].name}</br>
											${handMarking.handMarking.rowHeader[rowStatus.index].weight}
										</th>
										<c:forEach var="column" items="${handMarking.handMarking.columnHeader}">
											<td <c:if test="${result.handMarkingResults[handMarkingStatus.index].result[row.name] == column.name}" > class="handMarkingHighlight" </c:if>>
												<c:if test="${not empty handMarking.handMarking.data[column.name][row.name] or handMarking.handMarking.data[column.name][row.name] == \"\"}">
													<span><fmt:formatNumber type="number" maxIntegerDigits="3" value="${row.weight * column.weight}" /></span>
													</br>
													${handMarking.handMarking.data[column.name][row.name]}</br>
												</c:if>
											</td>
										</c:forEach>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</c:forEach>
			</c:if>
			<h5>Comments</h2>
			<div id="comments${assessment.shortName}">
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
	</c:when>
	
	<c:otherwise>
		<h3>Assessment not attempted</h3>
	</c:otherwise>
</c:choose>