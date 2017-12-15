<%--
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
--%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<h1> Hand Marking Template - ${handMarking.name}</h1>

<style>
th, td{
	min-width:200px;
}
</style>

<form:form commandName="updateHandMarkingForm" enctype="multipart/form-data" method="POST">
	<div class='section'>
		<h2 class='section-title'>Details</h2>
		<div class='part pasta-form'>
			<div class='pf-item one-col'>
				<div class='pf-label'>Name</div>
				<div class='pf-input'>
					<form:input path="name" />
					<form:errors path="name" />
				</div>
			</div>
			<div class='button-panel'>
				<button type="submit" onclick="updateRows();updateColumns();updateCells();">Save changes</button>
			</div>
		</div>
	</div>
	<div class='section part'>
		<div id="handMarkingTable" class='pasta-form wide banded'>
			<div class='pf-horizontal four-col'>
				<div class='pf-item'></div>
				<c:forEach var="column" items="${allColumns}" varStatus="columnStatus">
					<div class='pf-item compact column header'>
						<form:errors path="newColumnHeader[${columnStatus.index}].name" element="div" />
						<div class='pf-item'>
							<div class='pf-label'>
								Name
								<div class='pf-item float-right'>
									<button type="button" class="flat delButton delButtonDelColumn">Delete</button>
									<button type="button" class='confButton confButtonDelColumn' style="display:none;">Confirm</button>
								</div>
							</div>
							<div class='pf-input'>
								<form:input type="text" path="newColumnHeader[${columnStatus.index}].name" value="${column.name}"/>
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Weight</div>
							<div class='pf-input'>
								<form:input type="text" path="newColumnHeader[${columnStatus.index}].weight" value="${column.weight}"/>
							</div>
						</div>
						<form:input type="hidden" path="newColumnHeader[${columnStatus.index}].id" value="${column.id}"/>
					</div>
				</c:forEach>
			</div>
			<c:forEach var="row" items="${allRows}" varStatus="rowStatus">
				<div class='pf-horizontal four-col'>
					<div class='pf-item compact row header'> 
						<form:errors path="newRowHeader[${rowStatus.index}].name" element="div" />
						<div class='pf-item'>
							<div class='pf-label'>
								Name
								<div class='pf-item float-right'>
									<button type="button" class="flat delButton delButtonDelRow">Delete</button>
									<button type="button" class="confButton confButtonDelRow" style="display:none;">Confirm</button>
								</div>
							</div>
							<div class='pf-input'>
								<form:input type="text" path="newRowHeader[${rowStatus.index}].name" value="${row.name}"/>
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Weight</div>
							<div class='pf-input'>
								<form:input type="text" path="newRowHeader[${rowStatus.index}].weight" value="${row.weight}"/>
							</div>
						</div>
						<form:input type="hidden" path="newRowHeader[${rowStatus.index}].id" value="${row.id}"/>
					</div>
					<c:forEach var="column" items="${handMarking.columnHeader}">
						<div id="cell_${column.id}_${row.id}" class="emptyCell pf-item compact cell"><%-- to be filled by JavaScript --%></div>
					</c:forEach>
				</div>
			</c:forEach>
		</div>
		<div class='button-panel'>
			<button type="submit" onclick="updateRows();updateColumns();updateCells();">Save changes</button>
			<button type="button" class='flat' onclick="addColumn();">More columns</button>
			<button type="button" class='flat' onclick="addRow()">More rows</button>
		</div>
	</div>
</form:form>


<script src='<c:url value="/static/scripts/assessment/viewHandMarking.js"/>'></script>
<script>
	var newDataPosition = ${fn:length(handMarking.data)}
	
	function fillCells() {
		var cell;
		<c:forEach var="datum" items="${allData}" varStatus="datumStatus">
			cell = $("#cell_${datum.column.id}_${datum.row.id}");
			<c:choose>
			<c:when test="${not empty datum.data or datum.data == \"\"}">
				var errorData = <c:out value="'" escapeXml="false" /><form:errors path="updateHandMarkingForm.newData[${datumStatus.index}].data"/><c:out value="'"  escapeXml="false" />;
				fillCell(cell, 
					<fmt:formatNumber type='number' maxIntegerDigits='3' value='${datum.row.weight * datum.column.weight}' />,
					"${datumStatus.index}",
					"${datum.id}",
					"<c:out value='${pasta:escapeNewLines(datum.data)}' />",
					$(errorData));
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