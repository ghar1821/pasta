<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="now" class="java.util.Date"/>


<h1>${unikey.username}</h1>

<c:forEach var="assessmentCategory" items="${assessments}">
	<h2>${assessmentCategory.key}</h2>
	<table class="pastaQuickFeedback">
		<c:forEach var="assessment" items="${assessmentCategory.value}">
			<tr
				<c:if test="${assessment.dueDate lt now}">
					class="closedAssessment"
				</c:if>
					>
				<td>
					<a href="../info/${assessment.shortName}/">${assessment.name}</a> - 
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
							<c:if test="${not allUnitTests.secret or ((not empty viewedUser.extensions[assessment.shortName] and viewedUser.extensions[assessment.shortName] lt now) or (assessment.dueDate lt now))}">
								<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
									<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
								</c:forEach>
							</c:if>
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
		</c:forEach>
	</table>
</c:forEach>

<div id="submitPopup" class="popup">
	<form:form commandName="submission" enctype="multipart/form-data" method="POST">
		<span class="button bClose"> <span><b>X</b></span></span>
		By submitting this assessment I accept the unviersity's <a href="http://sydney.edu.au/engineering/it/current_students/undergrad/policies/academic_honesty.shtml">academic honesty policy.</a> </br>
		<form:input type="file" path="file"/>
		<form:input type="hidden" path="assessment" value=""/>
	   	<input type="submit" value="I accept" id="submit"/>
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
</script>