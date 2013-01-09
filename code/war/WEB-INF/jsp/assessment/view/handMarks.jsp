<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="pasta.domain.template.Tuple"%>

<h1> Hand Marking Template - ${handMarking.name}</h1>

<form:form commandName="handMarking" enctype="multipart/form-data" method="POST">
	<input type="submit" value="Save changes" id="submit" onclick="updateRows();updateColumns();updateCells();" style="margin-top:1em;"/>

	<table id="handMarkingTable">
		<thead>
			<tr>
				<th class="notdraggable"></th> <!-- empty on purpose -->
				<c:forEach items="${handMarking.columnHeader}" varStatus="columnStatus">
					<th>
						<form:input type="text" path="columnHeader[${columnStatus.index}].name"/></br>
						<form:input type="text" path="columnHeader[${columnStatus.index}].weight"/>
						<div class="button" style="text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete Column</div>
						<div class="button" style="display:none; text-align: center; " onclick="deleteColumn(this.parentNode.cellIndex);updateColumns()" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Comfirm</div>
					</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody class="sortable">
			<c:forEach var="row" items="${handMarking.rowHeader}" varStatus="rowStatus">
				<tr>
					<th>
						<form:input type="text" path="rowHeader[${rowStatus.index}].name"/></br>
						<form:input type="text" path="rowHeader[${rowStatus.index}].weight"/>
						<div class="button" style="text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete Row</div>
						<div class="button" style="display:none; text-align: center; " onclick="deleteRow(this.parentNode.parentNode.rowIndex);updateRows()" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Comfirm</div>
					</th>
					<c:forEach var="column" items="${handMarking.columnHeader}">
						<td><span><fmt:formatNumber type="number" maxIntegerDigits="3" value="${row.weight * column.weight}" /></span>
							</br>
							<form:textarea style="height:90%; width:95%" path="data['${column.name}']['${row.name}']"/></br>
						</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	
	<input type="submit" value="Save changes" id="submit" onclick="updateRows();updateColumns();updateCells();" style="margin-top:1em;"/>
</form:form>

<button onClick="addColumn()">More columns</button>
<button onClick="addRow()">More rows</button>

<script>
	function addColumn(){
		var table=document.getElementById("handMarkingTable");
		var tblHeadObj = table.tHead;
		for (var i=0; i<tblHeadObj.rows.length; i++) {
			var newTH = document.createElement('th');
			tblHeadObj.rows[i].appendChild(newTH);
			newTH.innerHTML = '<input id="columnHeader'+(table.rows[0].cells.length-2)+'.name" name="columnHeader['+(table.rows[0].cells.length-2)+'].name" type="text" type="text" value=""/></br><input id="columnHeader'+(table.rows[0].cells.length-2)+'.weight" name="columnHeader['+(table.rows[0].cells.length-2)+'].weight" type="text" type="text" value=""/><div class="button" style="text-align: center; " onclick="$(this).slideToggle(\'fast\').next().slideToggle(\'fast\')">Delete Column</div><div class="button" style="display:none; text-align: center; " onclick="deleteColumn(this.parentNode.cellIndex);updateColumns()" onmouseout="$(this).slideToggle(\'fast\').prev().slideToggle(\'fast\');">Comfirm</div>'
		}

		var tblBodyObj = document.getElementById("handMarkingTable").tBodies[0];
		for (var i=0; i<tblBodyObj.rows.length; i++) {
			var newCell = tblBodyObj.rows[i].insertCell(-1);
			newCell.innerHTML = '???</br><textarea id="data\'???\'\'???\'" name="data[\'???\'][\'???\']" style="height:90%; width:95%"></textarea></br>'
		}
	}
	
	function deleteColumn(column){
		var table=document.getElementById("handMarkingTable");
		for (var i=0; i<table.rows.length; i++) {
			table.rows[i].deleteCell(column);
		}
	}
	
	function updateColumns(){
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows[0].cells.length; i++) {
		
			var currHeader = table.rows[0].cells[i].getElementsByTagName("input");
			
			currHeader[0].id="columnHeader"+(i-1)+".name";
			currHeader[0].name="columnHeader["+(i-1)+"].name";
			
			currHeader[1].id="columnHeader"+(i-1)+".weight";
			currHeader[1].name="columnHeader["+(i-1)+"].weight";
			
			currHeader[1].setAttribute("value", parseFloat(currHeader[1].value));
		}
	}
	
	function addRow(){
		var table=document.getElementById("handMarkingTable");
		table.insertRow(table.rows.length)
		var newTH = document.createElement('th');
		table.rows[table.rows.length-1].appendChild(newTH);
		// -2 since the top corner should not be counter and length is 1 greater than index
		newTH.innerHTML = '<input id="rowHeader'+(table.rows.length-2)+'.name" name="rowHeader['+(table.rows.length-2)+'].name" type="text" type="text" value=""/></br><input id="rowHeader'+(table.rows.length-2)+'.weight" name="rowHeader['+(table.rows.length-2)+'].weight" type="text" type="text" value=""/><div class="button" style="text-align: center; " onclick="$(this).slideToggle(\'fast\').next().slideToggle(\'fast\')">Delete Row</div><div class="button" style="display:none; text-align: center; " onclick="deleteRow(this.parentNode.parentNode.rowIndex);updateRows()" onmouseout="$(this).slideToggle(\'fast\').prev().slideToggle(\'fast\');">Comfirm</div>'
		for (var i=1; i<table.rows[0].cells.length; i++) {
			var newCell = table.rows[table.rows.length-1].insertCell(i);
			newCell.innerHTML = '???</br><textarea id="data\'???\'\'???\'" name="data[\'???\'][\'???\']" style="height:90%; width:95%"></textarea></br>'
		}
	}
	
	function deleteRow(row){
		document.getElementById("handMarkingTable").deleteRow(row);
	}
	
	function updateRows(){
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows.length; i++) {
			var currHeader = table.rows[i].cells[0].getElementsByTagName("input");
			
			currHeader[0].id="rowHeader"+(i-1)+".name";
			currHeader[0].name="rowHeader["+(i-1)+"].name";
			
			currHeader[1].id="rowHeader"+(i-1)+".weight";
			currHeader[1].name="rowHeader["+(i-1)+"].weight";
			
			var currDelButton = table.rows[i].cells[0].getElementsByClassName("button");
		}
	}
	
	function updateCells(){
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows.length; i++) {
			var rowHeader = table.rows[i].cells[0].getElementsByTagName("input")[0].value;
			var rowValue = table.rows[i].cells[0].getElementsByTagName("input")[1].value;
			for (var j=1; j<table.rows[i].cells.length; j++) {
				var columnHeader = table.rows[0].cells[j].getElementsByTagName("input")[0].value;
				var columnValue = table.rows[0].cells[j].getElementsByTagName("input")[1].value;
				
				var currCell = table.rows[i].cells[j].getElementsByTagName("textarea")[0];
				currCell.id="data'"+columnHeader+"''"+rowHeader+"'";
				currCell.name="data['"+columnHeader+"']['"+rowHeader+"']";
				
				var currCellValue = table.rows[i].cells[j].getElementsByTagName("span")[0];
				currCellValue.innerHTML=(rowValue*columnValue).toPrecision(3);
			}
		}
	}	
	
    $(function() {
		$( "tbody.sortable" ).sortable({
	        connectWith: "tbody",
	        dropOnEmpty: true,
	        
	        stop: function(event, ui){
	        	updateCells();
	        }
	    });

    });
    
	$("input").blur(function () {
         updateColumns();
		 updateRows();
		 updateCells();
		 		 
		 // sort columns
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows[0].cells.length; i++) {
			var row1Value = table.rows[0].cells[i].getElementsByTagName("input")[1].value;
			for (var j=i+1; j<table.rows[0].cells.length; j++) {
				var row2Value = table.rows[0].cells[j].getElementsByTagName("input")[1].value;
				if(parseFloat(row1Value) > parseFloat(row2Value))	{
					swapColumn(i, j);
				}
			}
		}
		
    });
	
	function swapColumn(column1Index, column2Index){
		var table=document.getElementById("handMarkingTable");
		for (var i=0; i<table.rows.length; i++) {
			var tempHTML = table.rows[i].cells[column1Index].innerHTML;
			table.rows[i].cells[column1Index].innerHTML = table.rows[i].cells[column2Index].innerHTML;
			table.rows[i].cells[column2Index].innerHTML = tempHTML;
		}
	}
	
</script>