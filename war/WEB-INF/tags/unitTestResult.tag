<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<%@ attribute name="results" required="true" type="pasta.domain.result.AssessmentResult" rtexprvalue="true"%>
<%@ attribute name="closedAssessment" required="true" type="Boolean" rtexprvalue="true"%>

<%@ attribute name="separateGroup" required="false" type="Boolean"%>
<%@ attribute name="summary" required="false" type="Boolean"%>
<%@ attribute name="detailsLink" required="false" type="java.lang.String" rtexprvalue="true"%>

<c:if test="${summary}">
	<div class='vertical-block float-container'>
		<c:choose>
			<c:when test="${empty results or empty results.submissionsMade or results.submissionsMade == 0}">
				No attempts on record.
			</c:when>
			<c:when test="${results.error}">
				<div class="ui-state-error ui-corner-all">
					<div class='vertical-block align-contents-middle'>
						<div class='horizontal-block float-container'><span class="ui-icon ui-icon-alert float-left"></span></div>
						<div class='horizontal-block'>
							<strong>
								<c:out value="${results.errorReason}" default="No error details available" />
								<c:if test="${not empty detailsLink and (results.compileError or results.validationError)}"> - <a href="${detailsLink}">More details </a></c:if>
							</strong>
						</div>
					</div>
				</div>
			</c:when>
			<c:when test="${not empty results.assessment.allUnitTests and results.waitingToRun}">
				<div class="ui-state-highlight ui-corner-all">
					<span class="ui-icon ui-icon-info float-left"></span>&nbsp;
					<b><span class='queueInfo' assessment='${results.assessment.id}'>Submission is queued for testing.</span></b>
				</div>
			</c:when>
			<c:when test="${not empty results.assessment.allUnitTests}">
				<c:if test="${separateGroup}">
					<div class='align-contents-top'>
						<c:if test="${not empty results.groupUnitTests}">
							<div class='horizontal-block' style='max-width:50%'>
								<c:if test="${not empty results.nonGroupUnitTests}">
									<strong>Group Work:</strong>
								</c:if>
								<tag:unitTestResultBoxes closedAssessment="${closedAssessment}" 
									tests="${results.groupUnitTests}" />
							</div>
						</c:if>
						<c:if test="${not empty results.nonGroupUnitTests}">
							<div class='horizontal-block' style='max-width:50%'>
								<c:if test="${not empty results.groupUnitTests}">
									<strong style='white-space: nowrap'>Individual Work:</strong>
								</c:if>
								<tag:unitTestResultBoxes closedAssessment="${closedAssessment}" 
									tests="${results.nonGroupUnitTests}" />
							</div>
						</c:if>
					</div>
				</c:if>
				<c:if test="${!separateGroup}">
					<tag:unitTestResultBoxes closedAssessment="${closedAssessment}" 
						tests="${results.unitTests}" />
				</c:if>
			</c:when>
			<c:otherwise>
				No automatic testing results.<c:if test="${not empty detailsLink}"> Click <a href="${detailsLink}">here</a> for more details.</c:if>
			</c:otherwise>
		</c:choose>
	</div>
</c:if>

<c:if test="${!summary}">
	<div class='unitTestDetails vertical-block'>
		<c:choose>
			<c:when test="${empty results or empty results.submissionsMade or results.submissionsMade == 0}">
					No attempts on record.
			</c:when>
			<c:when test="${results.error}">
				<div class="ui-state-error ui-corner-all">
					<div class='vertical-block align-contents-middle'>
						<div class='horizontal-block float-container'><span class="ui-icon ui-icon-alert float-left"></span></div>
						<div class='horizontal-block'><strong><c:out value="${results.errorReason}" default="No error details available" /></strong></div>
					</div>
					<c:choose>
						<c:when test="${results.validationError}">
							<div class='vertical-block'>
								<c:forEach var="errorList" items="${results.validationErrors}">
									<c:if test="${not empty errorList.key}">
										<p><strong><c:out value="${errorList.key}" /></strong>
									</c:if>
									<ul>
										<c:forEach var="error" items="${errorList.value}">
											<li><c:out value="${error}" />
										</c:forEach>
									</ul>
								</c:forEach>
							</div>
						</c:when>
						<c:when test="${results.compileError}">
							<c:forEach var="compError" items="${results.compilationErrors}">
								<div class="vertical-block">
									<pre><c:out value="${compError}" escapeXml="true"/></pre>
								</div>
							</c:forEach>
						</c:when>
						<c:when test="${results.runtimeError}">
							<c:forEach var="runtimeError" items="${results.runtimeErrors}">
								<div class="vertical-block">
									<pre><c:out value="${runtimeError}" escapeXml="true"/></pre>
								</div>
							</c:forEach>
						</c:when>
					</c:choose>
				</div>
			</c:when>
			<c:when test="${not empty results.assessment.allUnitTests and results.waitingToRun}">
				<div class="ui-state-highlight">
					<b><span class='queueInfo' assessment='${results.assessment.id}'>Submission is queued for testing.</span></b>
				</div>
			</c:when>
			<c:when test="${not empty results.assessment.allUnitTests}">
				<table class='unitTestDetailsTable'>
					<thead>
						<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
					</thead>
					<tbody>
						<c:forEach var="unitTest" items="${results.unitTests}">
							<c:set var="secret" value="${unitTest.secret}"/>
							<c:set var="revealed" value="${secret and (user.tutor or closedAssessment)}"/>
							<c:set var="hidden" value="${secret and not revealed}"/>
						
							<c:forEach var="testCase" items="${unitTest.testCases}">
								<tr>
									<td>
										<span class="<c:out value='pastaUnitTestResult pastaUnitTestResult${hidden ? "Secret" : testCase.testResult}' />">
											<c:out value="${hidden ? '???' : testCase.testResult}" />
										</span>
									</td>
									<td>
										<c:if test="${hidden}">???</c:if>
										<c:if test="${!hidden}"><c:out value="${secret ? 'Secret - ' : ''}" />${testCase.testName}</c:if>
									</td>
									<td>
										<c:out value="${hidden ? '???' : testCase.time}" />
									</td>
									<td>
										<c:if test="${hidden}">???</c:if>
										<c:if test="${!hidden}">
											<pre><c:if test="${testCase.error}">${testCase.type} - </c:if><c:out value="${testCase.testMessage}"/></pre>
										</c:if>
									</td>
								</tr>
							</c:forEach>
						</c:forEach>
					</tbody>
				</table>
			</c:when>
			<c:otherwise>
				No automatic testing results.
			</c:otherwise>
		</c:choose>
	</div>
</c:if>