<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>${assessmentName} - ${student}</h1>

<form:form commandName="handMarkingResult" enctype="multipart/form-data" method="POST">
	<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>
	<table id="handMarkingTable">
		<thead>
			<tr>
				<th></th> <!-- empty on purpose -->
				<c:forEach items="${handMarking.columnHeader}" varStatus="columnStatus">
					<th style="cursor:pointer" onclick="clickAllInColumn(this.cellIndex)">
						${handMarking.columnHeader[columnStatus.index].name}</br>
						${handMarking.columnHeader[columnStatus.index].weight}</br>
					</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="row" items="${handMarking.rowHeader}" varStatus="rowStatus">
				<tr>
					<th>
						${handMarking.rowHeader[rowStatus.index].name}</br>
						${handMarking.rowHeader[rowStatus.index].weight}
					</th>
					<c:forEach var="column" items="${handMarking.columnHeader}">
						<td>
							<c:if test="${not empty handMarking.data[column.name][row.name] or handMarking.data[column.name][row.name] == \"\"}">
								<span><fmt:formatNumber type="number" maxIntegerDigits="3" value="${row.weight * column.weight}" /></span>
								</br>
								${handMarking.data[column.name][row.name]}</br>
								<form:radiobutton path="result['${row.name}']" value="${column.name}"/>
							</c:if>
						</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>
</form:form>

<script>
	function clickAllInColumn(column){
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows.length; i++) {
			var currHeader = table.rows[i].cells[column].getElementsByTagName("input");
			
			if(currHeader.length != 0){
				currHeader[0].checked = true;
			}
		}
	}
</script>