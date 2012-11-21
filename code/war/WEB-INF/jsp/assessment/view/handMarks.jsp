<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="pasta.domain.template.Tuple"%>

<h1> Rubric - ${handMarking.name}</h1>
${handMarking.description}
<table class="handMarkingTable">
	<col width="200px" /><tr><td></td>
	<c:forEach items="${handMarking.columns}" var="columnName">
		<th width ="200px" class="handMarkingTableElem"><c:out value="${columnName}" /></th><th class="handMarkingTableW"></th>
	</c:forEach>
	</tr>
<c:forEach items="${handMarking.data}" var="ArrayListColumn">
	<tr>
	<th><c:out value="${ArrayListColumn[0][3]}" /></th>
	<c:forEach items="${ArrayListColumn}" var="element">
	
	<td>
		<c:out value="${element[1]}" />
	</td>
	
	<td>
	  	<c:out value="${element[0]}" />
	</td>
	</c:forEach>
	</tr>
</c:forEach>
<table>