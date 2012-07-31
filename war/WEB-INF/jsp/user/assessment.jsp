<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1>${latestSubmission.name}</h1>
Due: ${latestSubmission.dueDate}</br>
Instructions: ${latestSubmission.instructions}</br>

<c:if test="${not empty latestSubmission.result}">
	<c:if test="${user.tutor}">
		<a href="../../download/${unikey}-${latestSubmission.name}">Download latest submission</a>
	</c:if>
	<h2> Latest submission: </h2>
	Submitted At: ${latestSubmission.submissionDate}
	</br>
	${latestSubmission.result}
	</br>
	${latestSubmission.feedback}
	${latestSubmission.junitTable}
	
	<c:if test="${not empty submissionHistory}">
		<h2> History </h2>
		<c:forEach var="assessment" items="${submissionHistory}">
			<h4>${assessment.submissionDate}</h4>
			${assessment.result}
			</br>
			${assessment.feedback}
			${assessment.junitTable}
		</c:forEach>
	</c:if>
</c:if>