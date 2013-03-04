<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
											<c:when test="${ empty result.handMarkingResults }">
												<button onClick="window.location.href='../../../../mark/${viewedUser.username}/${result.assessment.name}/${result.formattedSubmissionDate}/'" >Mark attempt</button>
											</c:when>
											<c:otherwise>
												<button onClick="window.location.href='../../../../mark/${viewedUser.username}/${result.assessment.name}/${result.formattedSubmissionDate}/'" >Edit attempt marks</button>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:choose>
											<c:when test="${ empty result.handMarkingResults }">
												<button onClick="window.location.href='../../mark/${unikey.username}/${result.assessment.name}/${result.formattedSubmissionDate}/'" >Mark attempt</button>
											</c:when>
											<c:otherwise>
												<button onClick="window.location.href='../../mark/${unikey.username}/${result.assessment.name}/${result.formattedSubmissionDate}/'" >Edit attempt marks</button>
											</c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</c:if>
							<!-- if the assessment contains unit tests -->
							<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
								<c:choose>
									<c:when test="${not empty viewedUser}">	
										<button onClick="window.location.href='../../../../runAssessment/${viewedUser.username}/${result.assessment.name}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
									</c:when>
									<c:otherwise>
										<button onClick="window.location.href='../../runAssessment/${unikey.username}/${result.assessment.name}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
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
												<pre>${testCase.type} - ${testCase.testMessage}</pre>
											</td>
										</tr>
									</c:forEach>
								</c:forEach>
							</table>
						</c:if>
						<h5>Comments</h2>
						<c:choose>
							<c:when test="${empty result.comments}">
								No comments
							</c:when>
							<c:otherwise>
								${result.comments}
							</c:otherwise>
						</c:choose>
					</div>
				</c:otherwise>
			</c:choose>
		</c:forEach>
	</c:when>
	
	<c:otherwise>
		<h3>Assessment not attempted</h3>
	</c:otherwise>
</c:choose>