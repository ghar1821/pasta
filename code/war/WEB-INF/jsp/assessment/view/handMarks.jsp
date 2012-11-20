<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="pasta.domain.template.Tuple"%>

<h1> Rubric - ${handMarking.name}</h1>
${handMarking.data[0][0][1]}
${handMarking.description}
<table>
	<tr>
	<c:forEach items="${handMarking.columns}" var="columnName">
		<th><c:out value="${columnName}" /></th>
	</c:forEach>
	</tr>
<c:forEach items="${handMarking.data}" var="ArrayListColumn">
	<tr>
	<c:forEach items="ArrayListColumn" var="element">
		
                        
		
	</c:forEach>
	</tr>
</c:forEach>
<table>