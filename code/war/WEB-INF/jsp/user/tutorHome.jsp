<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1>${unikey.username}</h1>

<h2>My Classes</h2>
<c:forEach var="class" items="${unikey.tutorClasses}">
	<a href="../tutorial/${class}/">${class}</a></br>
</c:forEach>
<button>Change My Classes</button>

<table class="pastaQuickFeedback">
	<c:forEach var="assessment" items="${assessments}">
		<form:form commandName="submission" enctype="multipart/form-data" method="POST">
			<tr <c:if test="${assessment.closed}">class="closedAssessment"</c:if> >
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
					${assessment.dueDate}</br>
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
				</td>
				<td>
					<div style="float: left; width:100%">
						<button type="button" style="float: left; text-align: center;"
							onClick="markBatch('${assessment.shortName}')">Mark my classes</button>
					</div>
				</td>
			</tr>
		</form:form>
	</c:forEach>
</table>

<div id="insertForm" style="display:none"></div>

<script>
	function markBatch(className){
		$('#insertForm').html('<form name="redirect" action="../mark/'+className+'/" method="post"><input type="text" name="currStudentIndex" value="0"/></form>')
		document.forms['redirect'].submit();
	}
</script>