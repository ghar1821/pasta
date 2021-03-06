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

<%@ taglib prefix="pasta" uri="pastaTag"%>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>


<jsp:useBean id="now" class="java.util.Date"/>

<c:set var="effectiveUser" value="${not empty viewedUser ? viewedUser : user}" />

<h1>
	<a href='../../home/'>${effectiveUser.username}</a> - ${assessment.name}
</h1>

<div class='info-panel'>
	<div class='ip-item'>
		<div class='ip-label'>Due Date</div>
		<div class='ip-desc'>
			<c:choose>
				<c:when test="${empty extension}"><pasta:readableDate date="${assessment.dueDate}" /></c:when>
				<c:otherwise>
					<span style='text-decoration: line-through;'><pasta:readableDate date="${assessment.dueDate}" /></span>
					<span style='color: red;'><pasta:readableDate date="${extension}" /></span>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<div class='ip-item'>
		<div class='ip-label'>Possible Marks</div>
		<div class='ip-desc'>${assessment.marks}</div>
	</div>
	<c:if test="${not empty assessment.submissionLanguages}">
		<div class='ip-item'>
			<div class='ip-label'>Allowed Languages</div>
			<div class='ip-desc'>
				<c:forEach var="language" items="${assessment.submissionLanguages}">
					<div><code>${language.name}</code></div>
				</c:forEach>
			</div>
		</div>
	</c:if>
</div>

<c:if test="${not empty assessment.description}">
	<h2>Assessment Description</h2>
	<div class='section'>
		<div class='show-math part'>
			${assessment.description}
		</div>
	</div>
</c:if>

<c:if test="${assessment.autoMarked}">
	<h2>General Submission Instructions</h2>
	<div class='section'>
		<c:if test="${not empty assessment.solutionName}">
			<div class='part'>
				<c:choose>
					<c:when test="${empty assessment.submissionLanguages}">
						Your submission must be in a file named <code>${assessment.solutionName}</code>.
					</c:when>
					<c:otherwise>
						Your submission must be written in <c:choose><c:when test="${fn:length(assessment.submissionLanguages) == 1}">the language</c:when><c:otherwise>one of the languages</c:otherwise></c:choose> listed above. It must include a main file named <code>${assessment.shortSolutionName}</code> (for example <code>${assessment.sampleSubmissionName}</code>).
					</c:otherwise>
				</c:choose>
			</div>
		</c:if>
		<c:choose>
			<c:when test="${fn:length(assessment.expectedDirectories) == 1}">
				<div class='part'>
					Your code submission must be in a directory called <code>${assessment.expectedDirectories[0]}</code>.
				</div>
			</c:when>
			<c:when test="${not empty assessment.expectedDirectories}">
				<div class='part'>
					Your code submission must contain the following code directories:
					<c:forEach var="dir" items="${assessment.expectedDirectories}" varStatus="loop">
						<code>${dir}</code><c:if test="${!loop.last}">, </c:if>
					</c:forEach>
				</div>
			</c:when>
		</c:choose>
		<div class='part'>
			<c:if test="${empty assessment.expectedDirectories}">If your submission contains multiple files, </c:if><strong>zip</strong> your submission into a <code>.zip</code> file and submit that. Do not use any other zip format (e.g. <code>.rar</code> or <code>.7z</code>).
		</div>
	</div>
</c:if>

<c:choose>
	<c:when test="${not empty history}">
		<h2>Submission History</h2>
		
		<c:forEach var="result" items="${history}" varStatus="resultStatus">
		<c:set var='groupResult' value="${result.user.id != effectiveUser.id}" />
		<div class='section'>
			<div class='clearfix section-title'>
				<h3 class='horizontal-block showHide' showhide='${result.id}'><pasta:readableDate date="${result.submissionDate}" /><c:out value="${lateString[result.id]}"/></h3>
				
				<%--Button panel--%>
				<c:if test="${ user.tutor }" >
					<div class='button-panel'>
						<c:set var="pathPrefix" value="../.." />
						<c:if test="${not empty viewedUser}">
							<c:set var="pathPrefix" value="../../../.." />
						</c:if>
						<c:if test="${not empty result.assessment.handMarking}">
							<c:set var="markButtonText" value="Mark attempt" />
							<c:if test="${not empty result.handMarkingResults and result.finishedHandMarking}">
								<c:set var="markButtonText" value="Edit attempt marks" />
							</c:if>
							<button class='flat hbn-button' data-hbn-icon="fa-check" onclick="window.location.href='${pathPrefix}/mark/${effectiveUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >${markButtonText}</button>
						</c:if>
						<c:if test="${not empty result.assessment.unitTests or not empty result.assessment.secretUnitTests}">
							<button class='flat hbn-button' data-hbn-icon="fa-refresh" onclick="window.location.href='${pathPrefix}/runAssessment/${effectiveUser.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'">Re-run attempt</button>
						</c:if>
						<button class='flat hbn-button' data-hbn-icon="fa-download" onclick="window.location.href='${pathPrefix}/download/${result.user.username}/${result.assessment.id}/${result.formattedSubmissionDate}/'" >Download attempt</button>
					</div>
				</c:if>
				
				<%--Group work submission details--%>
				<c:if test="${groupResult}">
					<div class='submitted-by-panel float-right'>
						<div class='align-contents-middle horizontal-block'>
							<div class='horizontal-block small-gap'><a>Group Submission</a></div>
							<div class='horizontal-block small-gap icon-group'></div>
							<div class='horizontal-block small-gap'>Submitted by ${result.submittedBy}.</div>
						</div>
					</div>
				</c:if>
			</div>
			
			<%--Submission contents--%>
			<div class='part no-line'>
				<div class='vertical-block'>
					<c:set var="node" value="${nodeList[result.formattedSubmissionDate]}" scope="request"/>
					<jsp:include page="../recursive/fileWriterRoot.jsp">
						<jsp:param name="owner" value="${result.user.group ? result.user.name : result.user.username}"/>
					</jsp:include>
				</div>
			</div>
			
			<%--Summary of results--%>
			<div class='part'>
				<h3 class='part-title'>Summary</h3>
				<tag:unitTestResult closedAssessment="${closed}" results="${result}" summary="true" />
			</div>
			
			<%--Details of results--%>
			<div id="${result.id}" class='resultDetails part no-line'>
				<h3 class='part-title'>Details</h3>
				
				<%--Files compiled--%>
				<c:if test="${ user.tutor and not empty result.unitTests}" >
					<div class='vertical-block'>
						<h4 class='showHide' showhide="files_${resultStatus.index}">Files Compiled</h4>
						<div id="files_${resultStatus.index}" class="ui-state-highlight ui-corner-all" style="display:none;padding:1em;padding-top:0;">
							<c:forEach var="unitTest" items="${result.unitTests}">
								<div>
									<h5>${unitTest.test.name}</h5>
									<pre><c:out value="${unitTest.filesCompiled}" escapeXml="true"/></pre>
								</div>
							</c:forEach>
						</div>
					</div>
				</c:if>
				
				<tag:unitTestResult closedAssessment="${closed}" results="${result}" />
				
				<tag:handMarkingResult results="${result}" marking="false"/>
				
				<div class='vertical-block'>
					<h4>Comments</h4>
					<div id="comments${result.id}" class='vertical-block'>
						<c:choose>
							<c:when test="${empty result.comments}">
								<em>No comments</em>
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
	</c:when>
	
	<c:otherwise>
		<h3>Assessment not attempted</h3>
	</c:otherwise>
</c:choose>
