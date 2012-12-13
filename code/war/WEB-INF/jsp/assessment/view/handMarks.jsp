<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="pasta.domain.template.Tuple"%>

<h1> Hand Marking Template - ${handMarking.name}</h1>

<form:form commandName="handMarking" enctype="multipart/form-data" method="POST">
	<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>

	<table>
		<tr>
			<td></td> <!-- empty on purpose -->
			<c:forEach items="${handMarking.columnHeader}" varStatus="columnStatus">
				<th>
					<form:input type="text" path="columnHeader[${columnStatus.index}].name"/></br>
					<form:input type="text" path="columnHeader[${columnStatus.index}].weight"/>
				</th>
			</c:forEach>
		</tr>
		<c:forEach var="row" items="${handMarking.rowHeader}" varStatus="rowStatus">
			<tr>
				<th>
					<form:input type="text" path="rowHeader[${rowStatus.index}].name"/></br>
					<form:input type="text" path="rowHeader[${rowStatus.index}].weight"/>
				</th>
				<c:forEach var="column" items="${handMarking.columnHeader}">
					<td>${row.weight * column.weight}
						</br>
						<form:textarea style="height:90%; width:95%" path="data['${column.name}']['${row.name}']"/></br>
					</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</table>
	
	<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>
</form:form>