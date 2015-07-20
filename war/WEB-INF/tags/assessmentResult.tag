<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<%@ attribute name="user" required="true" type="pasta.domain.user.PASTAUser" rtexprvalue="true"%>
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
			<c:when test="${results.compileError}">
				<div class="ui-state-error ui-corner-all">
					<span class="ui-icon ui-icon-alert float-left"></span>&nbsp;
					<b>Compilation errors<c:if test="${not empty detailsLink}"> - <a href="${detailsLink}">More details </a></c:if></b>
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
								<strong>Group Work:</strong>
								<tag:assessmentResultBoxes closedAssessment="${closedAssessment}" 
									user="${user}" tests="${results.groupUnitTests}" />
							</div>
						</c:if>
						<c:if test="${not empty results.nonGroupUnitTests}">
							<div class='horizontal-block' style='max-width:50%'>
								<strong style='white-space: nowrap'>Individual Work:</strong>
								<tag:assessmentResultBoxes closedAssessment="${closedAssessment}" 
									user="${user}" tests="${results.nonGroupUnitTests}" />
							</div>
						</c:if>
					</div>
				</c:if>
				<c:if test="${!separateGroup}">
					<tag:assessmentResultBoxes closedAssessment="${closedAssessment}" 
						user="${user}" tests="${results.unitTests}" />
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
			<c:when test="${results.compileError}">
				<div class="ui-state-error">
					<c:forEach var="compError" items="${results.compilationErrors}">
						<div class="vertical-block">
							<pre><c:out value="${compError}" escapeXml="true"/></pre>
						</div>
					</c:forEach>
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