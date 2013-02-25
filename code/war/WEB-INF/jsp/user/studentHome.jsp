<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<jsp:useBean id="now" class="java.util.Date"/>

<c:choose>
	<c:when test="${ not empty viewedUser}">
		<h1>${viewedUser.username}</h1>
	</c:when>
	<c:otherwise>
		<h1>${unikey.username}</h1>
	</c:otherwise>
</c:choose>

<table class="pastaQuickFeedback">
	<c:forEach var="assessment" items="${assessments}">
		<form:form commandName="submission" enctype="multipart/form-data" method="POST">
			<tr <c:choose>
					<c:when test="${not empty viewedUser.extensions[assessment.shortName]}">
						<c:if test="${viewedUser.extensions[assessment.shortName] lt now}">
							class="closedAssessment"
						</c:if>
					</c:when>
					<c:otherwise>
						<c:if test="${assessment.dueDate lt now}">
							class="closedAssessment"
						</c:if>
					</c:otherwise>
				</c:choose> >
				<td style="width:40px;">
					<form:input type="file" path="file"/>
					<form:input type="hidden" path="assessment" value="${assessment.shortName}"/>
			    	<input type="submit" value="Upload" id="submit"/>
				</td>
				<td>
					<a href="../info/${assessment.name}/">${assessment.name}</a> - 
					<fmt:formatNumber type="number" maxIntegerDigits="3" value="${results[assessment.shortName].marks}" />
					<c:if test="${empty results[assessment.shortName]}">
						0
					</c:if>
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
						${results[assessment.shortName].submissionsMade} of ${results[assessment.shortName].assessment.numSubmissionsAllowed} attempts made</br>
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
					<c:otherwise>
						<c:forEach var="allUnitTests" items="${results[assessment.shortName].unitTests}">
							<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
								<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
							</c:forEach>
						</c:forEach>
					</c:otherwise>
				</c:choose>
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
		</form:form>
	</c:forEach>
</table>

<c:if test="${ not empty viewedUser}">
	<div id="extensionPopup">
		<span class="button bClose"> <span><b>X</b></span>
		</span>
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
			window.location = "../extension/"+assessmentName+"/"+newDueDate;
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