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

<h6>Submission Instructions</h6>
<p>
To submit, <b>zip</b> your <i>src</i> folder and submit that zipped file.<br />
Your zip file should contain the src folder. If you are unsure, please email your tutor for an example.<br />
<div class="ui-state-error" style="font-size:1.5em">
<i><b>NOTE: These marks are provisional, your final marks will appear on eLearning and may differ from these marks.</b></i>
</div>
<br/><br/>

<c:forEach var="assessmentCategory" items="${assessments}">
	<c:if test="${not empty assessmentCategory.key}">
		<h2>${assessmentCategory.key}</h2>
	</c:if>
	<div class='pastaQuickFeedback' style='font-size:1.1em;'>
		<c:forEach var="assessment" items="${assessmentCategory.value}">
			<c:if test="${((not empty assessment.releasedClasses) and ( fn:contains(assessment.releasedClasses, classes))) or ((not empty assessment.specialRelease) and ( fn:contains(assessment.specialRelease, username)))}">
				<c:out value="${viewedUser.extensions[assessment.id]}"/>
				<c:set var="closedAssessment" value="false"/>
				<div class='vertical-block float-container' <c:choose>
						<c:when test="${not empty viewedUser.extensions[assessment.id]}">
							<c:if test="${viewedUser.extensions[assessment.id] lt now}">
								class="closedAssessment"
								<c:set var="closedAssessment" value="true"/>
							</c:if>
						</c:when>
						<c:when test="${not empty unikey.extensions[assessment.id]}">
							<c:if test="${unikey.extensions[assessment.id] lt now}">
								class="closedAssessment"
								<c:set var="closedAssessment" value="true"/>
							</c:if>
						</c:when>
						<c:otherwise>
							<c:if test="${assessment.dueDate lt now}">
								class="closedAssessment"
								<c:set var="closedAssessment" value="true"/>
							</c:if>
						</c:otherwise>
					</c:choose> >
					
					<div class='float-container'>
						
						<div class='horizontal-block float-left'>
							<a href="../info/${assessment.id}/">${assessment.name}</a> - 
							<c:choose>
								<c:when test="${empty results[assessment.id]}">
									0
								</c:when>
								<c:when test="${(not results[assessment.id].finishedHandMarking) or (not closedAssessment and not empty assessment.secretUnitTests)}">
									???
								</c:when>
								<c:otherwise>
									<fmt:formatNumber type="number" maxIntegerDigits="3" value="${results[assessment.id].marks}" />
								</c:otherwise>
							</c:choose>
							/ ${assessment.marks}
							<br />
							<c:choose>
								<c:when test="${not empty viewedUser.extensions[assessment.id]}">
									${viewedUser.extensions[assessment.id]}
								</c:when>
								<c:otherwise>
									${assessment.dueDate}
								</c:otherwise>
							</c:choose>
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
								<button type="button" onclick="giveExtension('${assessment.id}', '${assessment.simpleDueDate}')">Give extension</button>
							</c:if>
							<button type="button" onclick="submitAssessment('${assessment.id}');">Submit</button>
						</div>
						
						<c:if test="${empty viewedUser && results[assessment.id].submissionsMade > 0}">
							<div class='horizontal-block float-right'>
								<form:form commandName="ratingForm" cssClass="ratingForm" action='../rating/saveRating/${username}/${assessment.id}/'>
									<div class='ratingStars'>
										<form:hidden path="rating" value="${ratingForms[assessment.id].rating}" />
									</div>
									<div id='extraComments${assessment.id}' class='popup'>
										<form:textarea path="comment" value="${ratingForms[assessment.id].comment}"/><br/>
										
										<c:if test="${ratingForms[assessment.id].tooHard}">checked="checked"</c:if>
										<label>Too Hard? <form:checkbox path="tooHard" checked='checked' /></label><br/>
										<input id='submitRating' type='submit' />
									</div>
									<div>
										<div style='float:left'><a class='showComments' assessment='${assessment.id}'>More feedback</a></div>
										<div style='float:right' id='confirmRating'></div>
									</div>
								</form:form>
							</div>
						</c:if>
					</div>
					
					<div class='float-clear float-container'>
						<div class='horizontal-block float-left'>
							<c:if test="${((results[assessment.id].finishedHandMarking) and (closedAssessment or empty assessment.secretUnitTests)) and results[assessment.id].percentage >= 0.75}">
								<img class='star-medal' alt="Good Job"
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
										<b>Compilation errors</b>
									</div>
								</c:when>
								<c:when test="${empty results[assessment.id].unitTests[0].testCases and (not empty assessment.unitTests or not empty assessment.secretUnitTests) and not empty results[assessment.id]}">
									<div class="ui-state-highlight ui-corner-all" style="font-size: 1em;">
										<span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
										<b>Code is queued for testing.</b>
									</div>
								</c:when>
								<c:otherwise>
									<c:forEach var="allUnitTests" items="${results[assessment.id].unitTests}">
										<c:choose>
											<c:when test="${allUnitTests.secret}">
												<c:choose>
													<c:when test="${unikey.tutor or ((assessment.dueDate lt now) and (empty viewedUser.extensions[assessment.id] or viewedUser.extensions[assessment.id] lt now))}">
														<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
															<div class="pastaUnitTestBoxResult pastaUnitTestBoxResultSecret${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp;</div>
														</c:forEach>
													</c:when>
													<c:otherwise>
														<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
															<div class="pastaUnitTestBoxResult pastaUnitTestBoxResultSecret" title="???">&nbsp;</div>
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
								</c:otherwise>
							</c:choose>
						</div>
						
					</div>
				</div>
			</c:if>
		</c:forEach>
	</div>
</c:forEach>

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

<script>
	
		function submitAssessment(assessment){
			document.getElementById('assessment').value=assessment;
			$('#submitPopup').bPopup();
		}
		
		function setStars($starDiv, rating) {
			$starDiv.children("div").each(function() {
				if($(this).attr('rating') <= rating) {
					$(this).removeClass("emptyStar");
					$(this).addClass("fullStar");
				} else {
					$(this).removeClass("fullStar");
					$(this).addClass("emptyStar");
				}
			});
		}
		
		function resetStars($starDiv) {
			var $ratingInput = $starDiv.children("input");
			var rating = $ratingInput.val();
			rating = Math.max(0, Math.min(rating, 5));
			setStars($starDiv, rating)
		}
		
		$(document).ready(function() {
			$(".ratingForm").each(function() {
				var $form = $(this);
				var $starDiv = $form.children(".ratingStars");
				var $ratingInput = $starDiv.children("input");
				for(var i = 1; i <= 5; i++) {
					var $newDiv = 
						jQuery('<div/>', {
						    class: "emptyStar",
						    rating: i
						});
					$newDiv.on('click', function() {
						$ratingInput.val($(this).attr('rating'));
						$form.submit();
					});
					$newDiv.on('mouseover', function() {
						setStars($starDiv, $(this).attr('rating'));
					});
					$newDiv.on('mouseout', function() {
						resetStars($starDiv);
					});
					$starDiv.append($newDiv);
				}
				resetStars($starDiv);
			});
			
			$(".ratingForm").on('submit', function() {
				var $form = $(this);
				$form.find("#submitRating").prop('disabled', true);
				$.ajax({
					headers : {
						'Accept' : 'application/json',
					},
					url : $form.attr("action"),
					data : $form.serialize(),
					type : "POST",
					statusCode : {
						500 : function() {
							alert("Failed to send rating. Please try again later.");
						}
					},
					success : function() {
						$form.find("#confirmRating").html("<span style='color:green;'><strong>Sent!</strong></span>");
					}
				});
				return false;
			});
			
			$(".showComments").on('click', function() {
				$('#extraComments' + $(this).attr('assessment')).bPopup();
			});
		});
</script>

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
