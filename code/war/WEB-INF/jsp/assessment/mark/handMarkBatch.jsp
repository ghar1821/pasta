<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>${assessmentName} - ${student} (${currStudentIndex} of ${maxStudentIndex})</h1>

<ul class="list">
<jsp:include page="../../recursive/fileWriter.jsp"/>
</ul>

<style>
th, td{
	min-width:200px;
}
</style>
<c:choose>
	<c:when test="${not empty student}">
		<form:form commandName="assessmentResult" enctype="multipart/form-data" method="POST">
			<c:forEach var="handMarking" items="${handMarkingList}" varStatus="handMarkingStatus">
				<input type="hidden" name="currStudentIndex" value="${currStudentIndex+1}"/>
				<c:choose>
					<c:when test="${empty last}">
						<input type="submit" value="Save and continue" id="submit" style="margin-top:1em;"/>
					</c:when>
					<c:otherwise>
						<input type="submit" value="Save and exit" id="submit" style="margin-top:1em;"/>
					</c:otherwise>
				</c:choose>
				<form:input type="hidden" path="handMarkingResults[${handMarkingStatus.index}].handMarkingTemplateShortName" value="${handMarking.handMarking.shortName}"/>
				<div style="width:100%; overflow:auto">
					<table id="handMarkingTable${handMarkingStatus.index}" style="table-layout:fixed; overflow:auto">
						<thead>
							<tr>
								<th></th> <!-- empty on purpose -->
								<c:forEach items="${handMarking.handMarking.columnHeader}" varStatus="columnStatus">
									<th style="cursor:pointer" onclick="clickAllInColumn(this.cellIndex, ${handMarkingStatus.index})">
										${handMarking.handMarking.columnHeader[columnStatus.index].name}</br>
										${handMarking.handMarking.columnHeader[columnStatus.index].weight}</br>
									</th>
								</c:forEach>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="row" items="${handMarking.handMarking.rowHeader}" varStatus="rowStatus">
								<tr>
									<th>
										${handMarking.handMarking.rowHeader[rowStatus.index].name}</br>
										${handMarking.handMarking.rowHeader[rowStatus.index].weight}
									</th>
									<c:forEach var="column" items="${handMarking.handMarking.columnHeader}">
										<td style="cursor:pointer" onclick="clickCell(this.cellIndex, this.parentNode.rowIndex ,${handMarkingStatus.index})">
											<c:if test="${not empty handMarking.handMarking.data[column.name][row.name] or handMarking.handMarking.data[column.name][row.name] == \"\"}">
												<span><fmt:formatNumber type="number" maxIntegerDigits="3" value="${row.weight * column.weight}" /></span>
												</br>
												${handMarking.handMarking.data[column.name][row.name]}</br>
												<form:radiobutton path="handMarkingResults[${handMarkingStatus.index}].result['${row.name}']" value="${column.name}"/>
											</c:if>
										</td>
									</c:forEach>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</c:forEach>
			
			<form:textarea style="height:200px; width:95%" path="comments"/>
			<c:choose>
				<c:when test="${empty last}">
					<input type="submit" value="Save and continue" id="submit" style="margin-top:1em;"/>
				</c:when>
				<c:otherwise>
					<input type="submit" value="Save and exit" id="submit" style="margin-top:1em;"/>
				</c:otherwise>
			</c:choose>
			
		</form:form>
	</c:when>
	<c:otherwise>
		<b>No submissions left to mark</b>
	</c:otherwise>
</c:choose>


<script>
	function clickAllInColumn(column, tableIndex){
		var table=document.getElementById("handMarkingTable"+tableIndex);
		for (var i=1; i<table.rows.length; i++) {
			var currHeader = table.rows[i].cells[column].getElementsByTagName("input");
			
			if(currHeader.length != 0){
				currHeader[0].checked = true;
			}
		}
	}
	
	function clickCell(column,  row, tableIndex){
		var table=document.getElementById("handMarkingTable"+tableIndex);
		var currHeader = table.rows[row].cells[column].getElementsByTagName("input");
			
		if(currHeader.length != 0){
			currHeader[0].checked = true;
		}
	}
</script>