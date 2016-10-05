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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<div class='float-container'>
	<div class='horizontal-block'>
		<h1>Assessments</h1>
	</div>
	<input id='search' type='text' />
</div>

<div class='vertical-block float-container' style='width:100%'>
	<c:forEach var="assessmentCategory" items="${allAssessments}">
		<div class='section category-box'> <!-- Find category-box and fix -->
			<c:if test="${not empty assessmentCategory.key}">
				<h2 class='section-title'>${assessmentCategory.key}</h2>
			</c:if>
			<c:forEach var="assessment" items="${assessmentCategory.value}">
				<div class='part assessment-row'>
					<div class='part-title larger-text'>
						<a class='assessment-name' href="${assessment.id}/">${assessment.name}</a>
						<div class='horizontal-block'>
							<c:if test="${not assessment.completelyTested}">
								<div class='float-left'>
									<span class="ui-icon ui-icon-alert" title="Contains untested unit tests." ></span>
								</div>
							</c:if>
							<c:if test="${assessment.closed}">
								<div class='float-left'>
									<span class="ui-icon ui-icon-locked" title="Past due date"></span>
								</div>
							</c:if> 
							<c:if test="${not assessment.released}">
								<div class='float-left'>
									<span class="ui-icon ui-icon-gear" title="Not released"></span>
								</div>
							</c:if>
						</div>
					</div>
					<div>
						<div class='info-panel horizontal-block top-align'>
							<div class='ip-item'>
								<div class='ip-label'>Marks</div>
								<div class='ip-desc'>
									<c:choose>
										<c:when test="${assessment.marks eq 0}">Ungraded</c:when>
										<c:otherwise>Out of ${assessment.marks}</c:otherwise>
									</c:choose>
								</div>
							</div>
							<div class='ip-item'>
								<div class='ip-label'>Due Date</div>
								<div class='ip-desc'><pasta:readableDate date="${assessment.dueDate}" /></div>
							</div>
							<div class='ip-item'>
								<div class='ip-label'>Submissions Allowed</div>
								<div class='ip-desc'>
									<c:choose>
										<c:when test="${assessment.numSubmissionsAllowed == 0}">&infin;</c:when>
										<c:otherwise>${assessment.numSubmissionsAllowed}</c:otherwise>
									</c:choose>
								</div>
							</div>
						</div>
						<div class='info-panel horizontal-block top-align'>
							<div class='ip-item'>
								<div class='ip-label'>Modules</div>
								<div class='ip-desc'>
									<c:choose>
										<c:when test="${not assessment.hasWork}">No modules</c:when>
										<c:otherwise>
											<c:set scope="request" var="ut" value="${fn:length(assessment.unitTests)}" />
											<c:if test="${ut > 0}">
												${ut} Unit Test${ut == 1 ? '' : 's'}<br />
											</c:if>
											<c:set scope="request" var="sut" value="${fn:length(assessment.secretUnitTests)}" />
											<c:if test="${sut > 0}">
												${sut} Secret Unit Test${sut == 1 ? '' : 's'}<br />
											</c:if>
											<c:set scope="request" var="hm" value="${fn:length(assessment.handMarking)}" />
											<c:if test="${hm > 0}">
												${hm} Hand marking template${hm == 1 ? '' : 's'}<br />
											</c:if>
											<c:set scope="request" var="com" value="${fn:length(assessment.competitions)}" />
											<c:if test="${com > 0}">
												${com} Competition${com == 1 ? '' : 's'}<br />
											</c:if>
										</c:otherwise>
									</c:choose>
								</div>
							</div>
						</div>
					</div>
					<div class='button-panel'>
						<c:if test="${fn:length(assessment.allUnitTests) != 0}">
							<button class='flat' style="float: left;"
								onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Re-run</button>
							<button class='flat' style="float: left; display: none;"
									onclick="location.href='./${assessment.id}/run/'"
									onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
						</c:if>
						<button class='flat' onclick="location.href='./downloadLatest/${assessment.id}/'">Download Latest Submissions</button>
						<button class='flat' onclick="location.href='../moss/view/${assessment.id}/'">MOSS</button>
						<c:if test="${user.instructor}">
							<button class='flat' class='deleteAssessment' assessment='${assessment.id}'>Delete</button>
						</c:if>
					</div>
				</div>
			</c:forEach>
		</div>
	</c:forEach>
</div>

<c:if test="${user.instructor}">
	<button id="newPopup">Add a new Assessment</button>
	
	<div id="newAssessment" class='popup'>
		<span class="button bClose"> <span><b>X</b></span>
		</span>
		<h1>New Assessment</h1>
		<form:form commandName="newAssessmentForm" enctype="multipart/form-data"
			method="POST">
			<table>
				<tr>
					<td>Assessment Name:</td>
					<td><form:input autocomplete="off" type="text" path="name" /> <form:errors path="name" /></td>
				</tr>
				<tr>
					<td>Assessment Marks:</td>
					<td><form:input type="text" path="marks" /> <form:errors path="marks" /></td>
				</tr>
				<tr>
					<td>Assessment DueDate:</td>
					<td><form:input type="text" path="strDate"/> <form:errors path="dueDate" /></td>
				</tr>
				<tr>
					<td>Maximum Number of allowed submissions:</td>
					<td><form:input type="text" path="maxSubmissions" /> <form:errors path="maxSubmissions" /></td>
				</tr>
			</table>
			<input type="submit" value="Create" id="submit" />
		</form:form>
	</div>
</c:if>

<div id="confirmPopup" class="popup">
	<span class="button bClose"> <span><b>X</b></span>
	</span>
	<h1>Are you sure you want to do that?</h1>
	<button id="confirmButton" onclick="">Confirm</button>
</div>

<script>
	;
	(function($) {

		// DOM Ready
		$(function() {

			$("#strDate").datetimepicker({
				timeformat : 'hh:mm',
				dateFormat : 'dd/mm/yy'
			});// TODO

			$('#newPopup').on('click', function(e) {
				// Prevents the default action to be triggered. 
				e.preventDefault();
				$('#newAssessment').bPopup();
			});
			
			$('.deleteAssessment').on('click', function(e) {
				e.preventDefault();
				var confirmResult = confirm("If you delete this assessment, you will also delete any submissions, ratings and groups for this assessment.");
				if(confirmResult) {
					var id = $(this).attr("assessment");
					if(id) {
						location.href='delete/' + id + '/'
					}
				}
			});
		});

		<spring:hasBindErrors name='newAssessmentForm'>
			$('#newAssessment').bPopup();
	    </spring:hasBindErrors>
	    
	    $(".category-box,.assessment-row").searchNode();
		$(".assessment-name").searchable();
		$(".category-box").find(".section-title").searchable();
		var searchBox = $("#search").searchBox();
	})(jQuery);
</script>
