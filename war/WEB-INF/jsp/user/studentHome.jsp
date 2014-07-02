<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="now" class="java.util.Date"/>

<c:choose>
	<c:when test="${ not empty viewedUser}">
		<h1>${viewedUser.username}</h1>
		<c:set var="classes" value="${viewedUser.stream}.${viewedUser.tutorial}"/>
		<c:set var="releaseUsername" value="${viewedUser.username}"/>
	</c:when>
	<c:otherwise>
		<h1>${unikey.username}</h1>
		<c:set var="classes" value="${unikey.stream}.${unikey.tutorial}"/>
		<c:set var="releaseUsername" value="${unikey.username}"/>
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
To submit, <b>zip</b> your <i>src</i> folder and submit that zipped file.</br>
Your zip file should contain the src folder. If you are unsure, please email your tutor for an example.</br>
<div class="ui-state-error" style="font-size:1.5em">
<i><b>NOTE: These marks are provisional, your final marks will appear on eLearning and may differ from these marks.</b></i>
</div>

<c:forEach var="assessmentCategory" items="${assessments}">
	<h2>${assessmentCategory.key}</h2>
	<table class="pastaQuickFeedback">
		<c:forEach var="assessment" items="${assessmentCategory.value}">
			<c:if test="${((not empty assessment.releasedClasses) and ( fn:contains(assessment.releasedClasses, classes))) or ((not empty assessment.specialRelease) and ( fn:contains(assessment.specialRelease, releaseUsername)))}">
				<c:out value="${viewedUser.extensions[assessment.shortName]}"/>
				<c:set var="closedAssessment" value="false"/>
				<tr <c:choose>
						<c:when test="${not empty viewedUser.extensions[assessment.shortName]}">
							<c:if test="${viewedUser.extensions[assessment.shortName] lt now}">
								class="closedAssessment"
								<c:set var="closedAssessment" value="true"/>
							</c:if>
						</c:when>
						<c:when test="${not empty unikey.extensions[assessment.shortName]}">
							<c:if test="${unikey.extensions[assessment.shortName] lt now}">
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
					<td>
						<c:choose>
							<c:when test = "${((results[assessment.shortName].finishedHandMarking) and (closedAssessment or empty assessment.secretUnitTests)) and results[assessment.shortName].percentage == 1 and results[assessment.shortName].submissionsMade == 1}">
								<div style="float:right;"><img src="<c:url value="/static/images/Diamond_Star.png"/>" alt="Good Job" height="42px" width="42px"></div>
							</c:when>
							<c:when test = "${((results[assessment.shortName].finishedHandMarking) and (closedAssessment or empty assessment.secretUnitTests)) and results[assessment.shortName].percentage == 1}">
								<div style="float:right;"><img src="<c:url value="/static/images/Gold_Star.png"/>" alt="Good Job" height="42px" width="42px"></div>
							</c:when>
							<c:when test = "${((results[assessment.shortName].finishedHandMarking) and (closedAssessment or empty assessment.secretUnitTests)) and results[assessment.shortName].percentage >= 0.85}">
								<div style="float:right;"><img src="<c:url value="/static/images/Silver_Star.png"/>" alt="Good Job" height="42px" width="42px"></div>
							</c:when>
							<c:when test = "${((results[assessment.shortName].finishedHandMarking) and (closedAssessment or empty assessment.secretUnitTests)) and results[assessment.shortName].percentage >= 0.75}">
								<div style="float:right;"><img src="<c:url value="/static/images/Bronze_Star.png"/>" alt="Good Job" height="42px" width="42px"></div>
							</c:when>
						</c:choose>
						<a href="../info/${assessment.shortName}/">${assessment.name}</a> - 
						<c:choose>
							<c:when test="${empty results[assessment.shortName]}">
								0
							</c:when>
							<c:when test="${(not results[assessment.shortName].finishedHandMarking) or (not closedAssessment and not empty assessment.secretUnitTests)}">
								???
							</c:when>
							<c:otherwise>
								<fmt:formatNumber type="number" maxIntegerDigits="3" value="${results[assessment.shortName].marks}" />
							</c:otherwise>
						</c:choose>
						/ ${assessment.marks}</br>
					<c:choose>
						<c:when test="${not empty viewedUser.extensions[assessment.shortName]}">
							${viewedUser.extensions[assessment.shortName]}
						</c:when>
						<c:otherwise>
							${assessment.dueDate}
						</c:otherwise>
					</c:choose>
					</br>
					<c:choose>
						<c:when test="${assessment.numSubmissionsAllowed == 0}">
							&infin; sumbissions allowed </br>
						</c:when>
						<c:otherwise>
							<c:if test="${empty results[assessment.shortName]}">
								0
							</c:if>
							${results[assessment.shortName].submissionsMade} of ${assessment.numSubmissionsAllowed} attempts made</br>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${results[assessment.shortName].submissionsMade == 0 or empty results[assessment.shortName]}">
							No attempts on record.
						</c:when>
						<c:when test="${results[assessment.shortName].compileError}">
							<div class="ui-state-error ui-corner-all" style="font-size: 1em;">
								<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
								<b>Compilation errors</b>
							</div>
						</c:when>
						<c:when test="${empty results[assessment.shortName].unitTests[0].testCases and (not empty assessment.unitTests or not empty assessment.secretUnitTests) and not empty results[assessment.shortName]}">
							<div class="ui-state-highlight ui-corner-all" style="font-size: 1em;">
								<span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
								<b>Code is queued for testing.</b>
							</div>
						</c:when>
						<c:otherwise>
							<c:forEach var="allUnitTests" items="${results[assessment.shortName].unitTests}">
								<c:choose>
									<c:when test="${allUnitTests.secret}">
										<c:choose>
											<c:when test="${unikey.tutor or ((assessment.dueDate lt now) and (empty viewedUser.extensions[assessment.shortName] or viewedUser.extensions[assessment.shortName] lt now))}">
												<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
													<div class="pastaUnitTestBoxResult pastaUnitTestBoxResultSecret${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
												</c:forEach>
											</c:when>
											<c:otherwise>
												<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
													<div class="pastaUnitTestBoxResult pastaUnitTestBoxResultSecret" title="???">&nbsp</div>
												</c:forEach>
											</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
											<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
										</c:forEach>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</c:otherwise>
					</c:choose>
					<td style="width:40px;">
						<button type="button" style="float: left; text-align: center;"
								onClick="submitAssessment('${assessment.shortName}');">Submit</button>
					</td>
					<c:if test="${ not empty viewedUser}">
						<!-- tutor is viewing a user and they may give out an extension -->
						<td>
							<div style="float: left; width:100%">
							<button type="button" style="float: left; text-align: center;"
								onClick="giveExtension('${assessment.shortName}', '${assessment.simpleDueDate}')">Give extension</button>
							</div>
						</td>
					</c:if>
				</tr>
			</c:if>
		</c:forEach>
	</table>
</c:forEach>

<div id="submitPopup" class="popup">
	<form:form commandName="submission" enctype="multipart/form-data" method="POST">
		<span class="button bClose"> <span><b>X</b></span></span>
		<div style="font-size:150%">
			By submitting this assessment I accept the University of Sydney's <a href="http://sydney.edu.au/engineering/it/current_students/undergrad/policies/academic_honesty.shtml">academic honesty policy.</a> </br></br>
		</div>
		<form:input accept="application/zip" type="file" path="file"/>
		<form:input type="hidden" path="assessment" value=""/>
	   	<input type="submit" value="I accept" id="submit"/>
   	</form:form>
</div>

<script>
	
		function submitAssessment(assessment){
			document.getElementById('assessment').value=assessment;
			$('#submitPopup').bPopup();
		}
</script>

<c:if test="${ not empty viewedUser}">
	<div id="extensionPopup" class="popup">
		<span class="button bClose"> <span><b>X</b></span></span>
		<h1>Give an extension to this assessment for this student.</h1>
		<input type="text" id="simpleDueDate" name="simpleDueDate" />
		<div style="display:none" id="assessmentName"></div>
		
		<button id="confirm" onClick="confirmExtension()">Confirm</button>
	</div>
	<script>
		function giveExtension(assessment, dueDate){
			document.getElementById('assessmentName').innerHTML=assessment;
			document.getElementById('simpleDueDate').value = dueDate;
			$('#extensionPopup').bPopup();

		}
		
		function confirmExtension(){
			var assessmentName = document.getElementById('assessmentName').innerHTML;
			var newDueDate = document.getElementById('simpleDueDate').value.replace("/", "-").replace("/", "-");
			window.location = "../extension/"+assessmentName+"/"+newDueDate+"/";
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
