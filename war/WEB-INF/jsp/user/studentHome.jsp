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

<c:set var="username" value="${ not empty viewedUser ? viewedUser.username : unikey.username }"/>
<h1>${username}</h1>
<c:choose>
	<c:when test="${ not empty viewedUser}">
		<c:set var="classes" value="${viewedUser.stream}.${viewedUser.tutorial}"/>
	</c:when>
	<c:otherwise>
		<c:set var="classes" value="${unikey.stream}.${unikey.tutorial}"/>
	</c:otherwise>
</c:choose>


<spring:hasBindErrors name="submission">
	<form:form commandName="submission" enctype="multipart/form-data" method="POST">
		<h3>Submission Errors</h3>
		<form:errors path="file" cssClass="ui-state-error" element="div" />
	</form:form>
</spring:hasBindErrors>

<div class="ui-state-error" style="font-size:1.5em">
<i><b>NOTE: These marks are provisional, your final marks will appear on eLearning and may differ from these marks.</b></i>
</div>

<br/><br/>

<div class="padded"> 
	<c:forEach var="assessmentCategory" items="${assessments}">
		<c:if test="${not empty assessmentCategory.key}">
			<h2>${assessmentCategory.key}</h2>
		</c:if>
		<div class='pastaQuickFeedback'>
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
								<c:choose>
									<c:when test="${empty results[assessment.id]}">
										0.0
									</c:when>
									<c:when test="${(not results[assessment.id].finishedHandMarking) or (not closedAssessment and not empty assessment.secretUnitTests)}">
										???
									</c:when>
									<c:otherwise>
										<fmt:formatNumber type="number" minFractionDigits="1" maxFractionDigits="3" value="${results[assessment.id].marks}" />
									</c:otherwise>
								</c:choose>
								/ <fmt:formatNumber type="number" minFractionDigits="1" maxFractionDigits="3" value="${assessment.marks}" />
								<br />
								Due: ${dueDates[assessment.id]}
								<c:if test="${(not empty viewedUser and not empty viewedUser.extensions[assessment.id]) or not empty unikey.extensions[assessment.id]}">
									(with extension)
								</c:if>
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
								<c:if test="${ not empty viewedUser}">
									<!-- tutor is viewing a user and they may give out an extension -->
									<button onclick="giveExtension('${assessment.id}', '${assessment.simpleDueDate}')">Give extension</button>
								</c:if>
								<c:if test="${unikey.tutor or not closedAssessment}">
									<button onclick="submitAssessment('${assessment.id}');">Submit</button>
								</c:if>
							</div>
							
							<c:if test="${empty viewedUser && results[assessment.id].submissionsMade > 0}">
								<div class='horizontal-block float-right'>
									<form:form commandName="ratingForm" cssClass="ratingForm${assessment.id}" action='../rating/saveRating/${username}/${assessment.id}/'>
										<form:hidden path="comment" value="${ratingForms[assessment.id].comment}" />
										<c:set var="rated" value="${ratingForms[assessment.id].rating != 0}" />
										<div class='vertical'>
											<div class='ratingVisToggle <c:if test="${!rated}">hidden</c:if>'>
												<a>Change rating</a>
											</div>
											<div class='ratingControls float-container ratingVisToggle <c:if test="${rated}">hidden</c:if>'>
												<div class='horizontal-block float-left'>
													<p><span class='labelEasy'>Easy</span>
												</div>
												<div class='ratingDots horizontal-block float-left'>
													<form:hidden path="rating" value="${ratingForms[assessment.id].rating}" />
												</div>
												<div class='horizontal-block float-left'>
													<p><span class='labelHard'>Hard</span>
												</div>
											</div>
										</div>
										<div class='vertical small-gap float-container'>
											<div class='float-left'><a class='showComments' assessment='${assessment.id}'>More feedback</a></div>
											<div class='float-right' id='confirmRating'></div>
										</div>
										<div id='extraComments${assessment.id}' class='popup'>
											<p><strong>Tell us what you think about this assessment:</strong><br/>
											<textarea class="ratingComment">${ratingForms[assessment.id].comment}</textarea><br/>
											<p><button class='ratingSubmit' assessment='${assessment.id}'>Submit</button>
										</div>
									</form:form>
								</div>
							</c:if>
						</div>
						
						<div class='float-clear float-container vertical-block small-gap'>
							<div class='horizontal-block float-left star-medal'>
								<c:if test="${((results[assessment.id].finishedHandMarking) and (closedAssessment or empty assessment.secretUnitTests)) and results[assessment.id].percentage >= 0.75}">
									<img  alt="Good Job"
										<c:choose>
											<c:when test = "${results[assessment.id].percentage == 1 and results[assessment.id].submissionsMade == 1}">
												src="<c:url value='/static/images/Diamond_Star.png'/>"
											</c:when>
											<c:when test = "${results[assessment.id].percentage == 1}">
												src="<c:url value='/static/images/Gold_Star.png'/>"
											</c:when>
											<c:when test = "${results[assessment.id].percentage >= 0.85}">
												src="<c:url value='/static/images/Silver_Star.png'/>"
											</c:when>
											<c:otherwise>
												src="<c:url value='/static/images/Bronze_Star.png'/>"
											</c:otherwise>
										</c:choose>
									 />
								</c:if>
							</div>
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
												<c:set var="revealed" value="${secret and (unikey.tutor or closedAssessment)}" />
												<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
													<div class="unitTestResult <c:if test="${not secret or revealed}">${unitTestCase.testResult}</c:if>
													<c:if test="${revealed}">revealed</c:if> <c:if test="${secret}">secret</c:if>" 
													title="<c:choose><c:when test="${revealed or not secret}">${unitTestCase.testName}</c:when><c:otherwise>???</c:otherwise></c:choose>">&nbsp;</div>
												</c:forEach>
											</c:forEach>
										</div>
									</c:otherwise>
								</c:choose>
							</div>
							<c:if test="${assessment.groupWork}">
								<div id="editGroup" class='horizontal-block float-right icon-edit-group' 
								title='Group Details' assessment='${assessment.id}'></div>
							</c:if>
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
		<form:input path="file" type="file" />
		<form:input type="hidden" path="assessment" value=""/>
	   	<button type="submit" onclick="this.disabled=true;this.innerHTML='Sending, please wait...';document.getElementById('submission').submit();" >I accept</button>
   	</form:form>
</div>

<script>
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
		
		$("#editGroup").on('click', function() {
			location.href = '../groups/' + $(this).attr('assessment') + '/';
		});
	});
</script>
<script src='<c:url value="/static/scripts/home/assessmentRatings.js"/>'></script>

<c:if test="${ not empty viewedUser}">
	<div id="extensionPopup" class="popup">
		<span class="button bClose"> <span><b>X</b></span></span>
		<h1>Give an extension to this assessment for this student.</h1>
		<input type="text" id="simpleDueDate" name="simpleDueDate" />
		<div style="display:none" id="assessmentId"></div>
		
		<button id="confirm" onclick="confirmExtension()">Confirm</button>
	</div>
	<script>
		function giveExtension(assessment, dueDate){
			document.getElementById('assessmentId').innerHTML=assessment;
			document.getElementById('simpleDueDate').value = dueDate;
			$('#extensionPopup').bPopup();

		}
		
		function confirmExtension(){
			var assessmentId = document.getElementById('assessmentId').innerHTML;
			var newDueDate = document.getElementById('simpleDueDate').value.replace("/", "-").replace("/", "-");
			window.location = "../extension/"+assessmentId+"/"+newDueDate+"/";
		}
		
		(function($) {
			// DOM Ready
			$(function() {
	
				$("#simpleDueDate").datetimepicker({
					timeformat : 'hh:mm',
					dateFormat : 'dd/mm/yy'
				});
			});
	
		})(jQuery);
	</script>
</c:if>
