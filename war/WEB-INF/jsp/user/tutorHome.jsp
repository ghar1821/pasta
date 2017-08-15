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
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<div class='float-container'>
	<div class='horizontal-block'>
		<h1>Home - ${user.username}</h1>
	</div>
	<input id='search' type='text' />
</div>

<spring:hasBindErrors name="submission">
	<div class='vertical-box padded'>
		<form:form commandName="submission" enctype="multipart/form-data" method="POST">
			<h3>Submission Errors</h3>
			<form:errors path="*" cssClass="ui-state-error" element="div" />
		</form:form>
	</div>
</spring:hasBindErrors>

<tag:submissionValidation />

<div class="vertical-box padded"> 
	<c:forEach var="assessmentCategory" items="${assessments}">
		<div class='section category-box'>
			<c:if test="${not empty assessmentCategory.key}">
				<h2 class='section-title'>${assessmentCategory.key}</h2>
			</c:if>
			<c:forEach var="assessment" items="${assessmentCategory.value}">
				<c:set var="closedAssessment" value="false"/>
				<c:if test="${closed[assessment.id]}">
					<c:set var="closedAssessment" value="true"/>
				</c:if>
				<div class='part assessment-box float-container <c:if test="${closedAssessment}">closedAssessment</c:if>' >
					<div class='part-title larger-text'>
						<a href="../info/${assessment.id}/">${assessment.name}</a>
					</div>
					
					<div class='clearfix vertical'>
						<div class='horizontal-block float-left'>
							<c:choose>
								<c:when test="${assessment.marks eq 0}">
									<div class='assessment-ungraded'>Ungraded</div>
								</c:when>
								<c:otherwise>
									<div class='assessment-mark'>
										<div class='mark-numerator'>
											<fmt:formatNumber type="number" minFractionDigits="0" maxFractionDigits="3" value="${results[assessment.id].marks}" />
											<c:if test="${empty results[assessment.id]}">
												0
											</c:if>
										</div>
										<div class='mark-separator'>out of</div>
										<div class='mark-denominator'>
											<fmt:formatNumber type="number" minFractionDigits="0" maxFractionDigits="3" value="${assessment.marks}" />
										</div>
									</div>
								</c:otherwise>
							</c:choose>
						</div>
						<div class='horizontal-block float-left'>
							<div class='info-panel'>
								<div class='ip-item'>
									<div class='ip-label'>Due:</div>
									<div class='ip-desc'>${dueDates[assessment.id]}</div>
								</div>
								<div class='ip-item'>
									<div class='ip-label'>Attempts:</div>
									<div class='ip-desc'>
										<c:if test="${empty results[assessment.id]}">
											0
										</c:if>
										${results[assessment.id].submissionsMadeThatCount} of 
										<c:choose>
											<c:when test="${assessment.numSubmissionsAllowed == 0}">
												&infin;
											</c:when>
											<c:otherwise>
												${assessment.numSubmissionsAllowed} 
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class='vertical'>
						<tag:unitTestResult results="${results[assessment.id]}" 
							closedAssessment="${closedAssessment}" summary="true" separateGroup="true"
							detailsLink="../info/${assessment.id}/"/>
					</div>
					
					<div class='button-panel'>
						<button onclick="submitAssessment('${assessment.id}', '${assessment.dueDate}', ${hasGroupWork[assessment.id]}, ${allGroupWork[assessment.id]});">Submit</button>
						<button class='flat' onclick="location.href='../info/${assessment.id}/'">Details</button>
						<c:if test="${ not empty user.tutorial and not empty assessment.handMarking}">
							<button class='flat' onclick="location.href='../mark/${assessment.id}/'">Mark my classes</button>
						</c:if>
						<c:if test="${assessment.groupWork}">
							<button class='flat' onclick="location.href='../groups/${assessment.id}/'">Group Details</button>
						</c:if>
					</div>
				</div>
			</c:forEach>
		</div>
	</c:forEach>
</div>

<div id="submitPopup" class="popup">
	<form:form commandName="submission" enctype="multipart/form-data" method="POST">
		<span class="button bClose"> <span><b>X</b></span></span>
		<form:input type="hidden" path="assessment" value=""/>
		
		<div id='lateNotice' class='vertical-block' style="text-align:center;font-size:200%; color:red">
			You are submitting this assessment late.
		</div>
		<div class='vertical-block' style="font-size:150%">
			By submitting this assessment I accept the University of Sydney's <a href="http://sydney.edu.au/engineering/it/current_students/undergrad/policies/academic_honesty.shtml">academic honesty policy.</a>
		</div>
		<div id='groupDeclaration' class='vertical-block' style="font-size:150%">
			By submitting I also declare that all group members have participated to a satisfactory level in completing this assessment.
		</div>
		<div class='vertical-block'>
			<form:input path="file" type="file" />
		</div>
		<div id='groupCheckDiv' class='vertical-block'>
			<form:checkbox id='groupCheck' cssClass="custom-check" path="groupSubmission"/>
			<label for='groupCheck' style="vertical-align: middle;"></label>
			<span style="font-size:150%; vertical-align: middle;">I am submitting on behalf of my group.</span>
		</div>
		<div class='vertical-block'>
		   	<button type="submit" onclick="this.disabled=true;this.innerHTML='Sending, please wait...';document.getElementById('submission').submit();" >I accept</button>
		</div>
   	</form:form>
</div>


<div id="insertForm" style="display:none"></div>

<script>
	function markBatch(className){
		$('#insertForm').html('<form name="redirect" action="../mark/'+className+'/" method="post"><input type="text" name="currStudentIndex" value="0"/></form>')
		document.forms['redirect'].submit();
	}
	
	function submitAssessment(assessment, dueDate, hasGroup, allGroup){
		document.getElementById('assessment').value=assessment;
		var $popup = $('#submitPopup');
		$popup.find("#groupCheckDiv").toggle(hasGroup);
		$popup.find("#groupCheck").prop("checked", allGroup).trigger("change");
		var late = new Date().getTime() > new Date(dueDate).getTime();
		$popup.find("#lateNotice").toggle(late);
		$popup.bPopup();
	}
	$("#groupCheck").on("change", function() {
		$("#groupDeclaration").toggle($(this).is(":checked"));
	});
	
	$(document).ready(function() {
		var assessmentIds = {};
		var $uniqueQueueInfo = $('.queueInfo').filter(function(){
		    var id = $(this).attr("assessment");
		    if(assessmentIds[id]){
		        return false;   
		    } else {
		        assessmentIds[id] = true;
		        return true;
		    }
		});
		
		$uniqueQueueInfo.each(function() {
			var assessmentId = $(this).attr("assessment");
			var $span = $('.queueInfo[assessment="' + assessmentId + '"]');
			(function checkQueue(timeout) {
				$.ajax({
					url : '../checkJobQueue/' + assessmentId + '/',
					success : function(data) {
						var done = false;
						if (data == "error") {
							$span.html("There was an error while running your submission.");
							done = true;
						} else if(data) {
							$span.html(data);
						} else {
							$span.html("Refresh for results.");
							refreshResults();
							done = true;
						}
						if(!done) {
							if(!timeout) {
								timeout = 0;
							}
							timeout += 3000;
							setTimeout(function() {
								checkQueue(timeout);
							}, timeout);
						}
					}
				});
			})();
			function refreshResults() {
				var container = $span.closest(".utr-top-level").parent();
				var url = '../utResults/' + assessmentId + '/';
				var data = {
						summary: true,
						separateGroup: true,
						detailsLink: "../info/" + assessmentId + "/"
				};
				container.load(url, data);
			}
		});
		
		$(".editGroup").on('click', function() {
			location.href = '../groups/' + $(this).attr('assessment') + '/';
		});
		
		$(".category-box,.assessment-box").searchNode();
		$(".category-box").find(".section-title").searchable();
		$(".assessment-box").find(".part-title").searchable();
		var searchBox = $("#search").searchBox();
	});
</script>
