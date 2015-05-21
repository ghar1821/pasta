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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="now" class="java.util.Date"/>


<h1>${unikey.username}</h1>

<spring:hasBindErrors name="submission">
	<form:form commandName="submission" enctype="multipart/form-data" method="POST">
		<h3>Submission Errors</h3>
		<form:errors path="file" cssClass="ui-state-error" element="div" />
	</form:form>
</spring:hasBindErrors>

<div class="padded"> 
	<c:forEach var="assessmentCategory" items="${assessments}">
		<c:if test="${not empty assessmentCategory.key}">
			<h2>${assessmentCategory.key}</h2>
		</c:if>
		<div class="pastaQuickFeedback">
			<div class='boxCard'>
				<c:forEach var="assessment" items="${assessmentCategory.value}">
					<c:set var="closedAssessment" value="false"/>
					<c:if test="${closed[assessment.id]}">
						<c:set var="closedAssessment" value="true"/>
					</c:if>
					<div class='boxCard vertical-block float-container <c:if test="${closedAssessment}">closedAssessment</c:if>' >
						<div class='float-container vertical-block'>
							<div class='horizontal-block float-left'>
								<a href="../info/${assessment.id}/">${assessment.name}</a> - 
								<fmt:formatNumber type="number" minFractionDigits="1" maxFractionDigits="3" value="${results[assessment.id].marks}" />
								<c:if test="${empty results[assessment.id]}">
									0.0
								</c:if>
								/ <fmt:formatNumber type="number" minFractionDigits="1" maxFractionDigits="3" value="${assessment.marks}" />
								<br />
								Due: ${dueDates[assessment.id]}
								<br />
								<c:choose>
									<c:when test="${assessment.numSubmissionsAllowed == 0}">
										&infin; submissions allowed <br />
									</c:when>
									<c:otherwise>
										<c:if test="${empty results[assessment.id]}">
											0
										</c:if>
										${results[assessment.id].submissionsMade} of ${assessment.numSubmissionsAllowed} attempts made<br />
									</c:otherwise>
								</c:choose>
							</div>
							
							<div class='horizontal-block float-right'>
								<c:if test="${ not empty unikey.tutorial and not empty assessment.handMarking}">
									<button onclick="location.href='../mark/${assessment.id}/'">Mark my classes</button>
								</c:if>
								<button onclick="submitAssessment('${assessment.id}');">Submit</button>
							</div>
						</div>
						
						<div class='float-clear float-container vertical-block small-gap'>
							<div class='horizontal-block float-left'>
								<c:choose>
									<c:when test="${results[assessment.id].submissionsMade == 0 or empty results[assessment.id]}">
										No attempts on record.
									</c:when>
									<c:when test="${results[assessment.id].compileError}">
										<div class="ui-state-error ui-corner-all" style="font-size: 1em;">
											<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
											<b>Compilation errors - <a href="../info/${assessment.id}/">More details </a></b>
										</div>
									</c:when>
									<c:when test="${empty results[assessment.id].unitTests[0].testCases and (not empty assessment.unitTests or not empty assessment.secretUnitTests) and not empty results[assessment.id]}">
										<div class="ui-state-highlight ui-corner-all" style="font-size: 1em;">
											<span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
											<b><span class='queueInfo' assessment='${assessment.id}'>Submission is queued for testing.</span></b>
										</div>
									</c:when>
									<c:otherwise>
										<strong>Latest results:</strong><br />
										<div class='float-container'>
											<c:forEach var="allUnitTests" items="${results[assessment.id].unitTests}">
												<c:set var="secret" value="${allUnitTests.secret}"/>
												<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
													<div class="unitTestResult ${unitTestCase.testResult} <c:if test="${secret}">revealed</c:if>"
													 title="${unitTestCase.testName}">&nbsp;</div>
												</c:forEach>
											</c:forEach>
										</div>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>
				</c:forEach>
			</div>
		</div>
	</c:forEach>
</div>

<div id="submitPopup" class="popup">
	<form:form commandName="submission" enctype="multipart/form-data" method="POST">
		<span class="button bClose"> <span><b>X</b></span></span>
		<div style="font-size:150%">
			By submitting this assessment I accept the University of Sydney's <a href="http://sydney.edu.au/engineering/it/current_students/undergrad/policies/academic_honesty.shtml">academic honesty policy.</a> <br /><br />
		</div>
		<form:input path="file" accept="application/zip" type="file" />
		<form:input type="hidden" path="assessment" value=""/>
	   	<button type="submit" onclick="this.disabled=true;this.innerHTML='Sending, please wait...';document.getElementById('submission').submit();" >I accept</button>
   	</form:form>
</div>


<div id="insertForm" style="display:none"></div>

<script>
	function markBatch(className){
		$('#insertForm').html('<form name="redirect" action="../mark/'+className+'/" method="post"><input type="text" name="currStudentIndex" value="0"/></form>')
		document.forms['redirect'].submit();
	}
	
	function submitAssessment(assessment){
		document.getElementById('assessment').value=assessment;
		$('#submitPopup').bPopup();
	}
	
	$(document).ready(function() {
		$(".queueInfo").each(function() {
			var $span = $(this);
			(function checkQueue() {
				var done = false;
				$.ajax({
					url : '../checkJobQueue/' + $span.attr("assessment") + '/',
					success : function(data) {
						if (data) {
							$span.html(data);
						} else {
							$span.html("Refresh for results.");
							done = true;
						}
					}
				});
				if(!done) {
					setTimeout(checkQueue, 3000);
				}
			})();
		});
	});
</script>
