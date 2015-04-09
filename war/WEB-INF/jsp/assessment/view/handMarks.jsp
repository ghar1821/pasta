<!-- 
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
-->

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="pasta.domain.template.WeightedField"%>

<h1> Hand Marking Template - ${handMarking.name}</h1>

<style>
th, td{
	min-width:200px;
}
</style>

<form:form commandName="updateHandMarkingForm" enctype="multipart/form-data" method="POST">
	<form:input type ="hidden" path="name" value="${handMarking.name}"/>
	<input type="submit" value="Save changes" id="submit" onclick="updateRows();updateColumns();updateCells();" style="margin-top:1em;"/>
<div style="width:100%; overflow:auto">
	<table id="handMarkingTable" >
		<thead>
			<tr>
				<th class="notdraggable"></th> <!-- empty on purpose -->
				<c:forEach var="column" items="${handMarking.columnHeader}" varStatus="columnStatus">
					<th>
						<form:input type="text" path="newColumnHeader[${columnStatus.index}].name" value="${column.name}"/><br />
						<form:input type="text" path="newColumnHeader[${columnStatus.index}].weight" value="${column.weight}"/>
						<form:input type="hidden" path="newColumnHeader[${columnStatus.index}].id" value="${column.id}"/>
						<div class="button" style="text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete Column</div>
						<div class="button" style="display:none; text-align: center; " onclick="deleteColumn(this.parentNode.cellIndex);updateColumns()" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</div>
					</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody class="sortable">
			<c:forEach var="row" items="${handMarking.rowHeader}" varStatus="rowStatus">
				<tr>
					<th>
						<form:input type="text" path="newRowHeader[${rowStatus.index}].name" value="${row.name}"/><br />
						<form:input type="text" path="newRowHeader[${rowStatus.index}].weight" value="${row.weight}"/>
						<form:input type="hidden" path="newRowHeader[${rowStatus.index}].id" value="${row.id}"/>
						<div class="button" style="text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete Row</div>
						<div class="button" style="display:none; text-align: center; " onclick="deleteRow(this.parentNode.parentNode.rowIndex);updateRows()" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</div>
					</th>
					<c:forEach var="column" items="${handMarking.columnHeader}">
						<td id="cell_${column.id}_${row.id}" class="emptyCell"><%-- to be filled by JavaScript --%></td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	</div>
	<input type="submit" value="Save changes" id="submit" onclick="updateRows();updateColumns();updateCells();" style="margin-top:1em;"/>
</form:form>

<button onclick="addColumn()">More columns</button>
<button onclick="addRow()">More rows</button>

<script src='<c:url value="/static/scripts/assessment/viewHandMarking.js"/>'></script>
<script>
	var newDataPosition = ${fn:length(handMarking.data)}
	
	function fillCells() {
		var cell;
		<c:forEach var="datum" items="${handMarking.data}" varStatus="datumStatus">
			cell = document.getElementById("cell_${datum.column.id}_${datum.row.id}");
			<c:choose>
			<c:when test="${not empty datum.data or datum.data == \"\"}">
				fillCell(cell, 
					<fmt:formatNumber type='number' maxIntegerDigits='3' value='${datum.row.weight * datum.column.weight}' />,
					"${datumStatus.index}",
					"${datum.id}",
					"${datum.column.id}",
					"${datum.row.id}",
					"${datum.data}");
			</c:when>
			<c:otherwise>
				fillEmptyCell(cell);
			</c:otherwise>
		</c:choose>
		</c:forEach>
		
		registerEvents();
	}
	
	$(function() {
		fillCells()
	});
</script>