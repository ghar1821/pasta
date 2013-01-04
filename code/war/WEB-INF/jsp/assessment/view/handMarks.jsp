<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="pasta.domain.template.Tuple"%>

<h1> Hand Marking Template - ${handMarking.name}</h1>

<form:form commandName="handMarking" enctype="multipart/form-data" method="POST">
	<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>

	<table id="handMarkingTable">
		<thead>
			<tr>
				<th class="notdraggable dragtable-drag-boundary"></th> <!-- empty on purpose -->
				<c:forEach items="${handMarking.columnHeader}" varStatus="columnStatus">
					<th>
						<div class="dragtable-drag-handle" style="width:100%; height: 30px; background-color:gray;"></div>
						<div class="button" style="float:right" onclick="deleteColumn(this.parentNode.cellIndex);updateColumns()">X</div>
						<form:input style="z-index:9000" type="text" path="columnHeader[${columnStatus.index}].name" onkeyup="updateCells()"/></br>
						<form:input type="text" path="columnHeader[${columnStatus.index}].weight"/>
					</th>
				</c:forEach>
			</tr>
		</thead>
		<tbody class="sortable">
			<c:forEach var="row" items="${handMarking.rowHeader}" varStatus="rowStatus">
				<tr>
					<th>
						<div class="button" style="float:right" onclick="deleteRow(this.parentNode.parentNode.rowIndex);updateRows()">X</div>
						<form:input type="text" path="rowHeader[${rowStatus.index}].name" onkeyup="updateCells()"/></br>
						<form:input type="text" path="rowHeader[${rowStatus.index}].weight"/>
					</th>
					<c:forEach var="column" items="${handMarking.columnHeader}">
						<td><fmt:formatNumber type="number" maxIntegerDigits="3" value="${row.weight * column.weight}" /></span>
							</br>
							<form:textarea style="height:90%; width:95%" path="data['${column.name}']['${row.name}']"/></br>
						</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	
	<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>
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
			newTH.innerHTML = '<input id="columnHeader'+(table.rows[0].cells.length-2)+'.name" name="columnHeader['+(table.rows[0].cells.length-2)+'].name" type="text" type="text" value=""/></br><input id="columnHeader'+(table.rows[0].cells.length-2)+'.weight" name="columnHeader['+(table.rows[0].cells.length-2)+'].weight" type="text" type="text" value=""/>'
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
		for (var i=1; i<table.rows.length; i++) {
			var currHeader = table.rows[0].cells[i].getElementsByTagName("input");
			
			currHeader[0].id="columnHeader"+(i-1)+".name";
			currHeader[0].name="columnHeader["+(i-1)+"].name";
			
			currHeader[1].id="columnHeader"+(i-1)+".weight";
			currHeader[1].name="columnHeader["+(i-1)+"].weight";
		}
	}
	
	function addRow(){
		var table=document.getElementById("handMarkingTable");
		table.insertRow(table.rows.length)
		var newTH = document.createElement('th');
		table.rows[table.rows.length-1].appendChild(newTH);
		// -2 since the top corner should not be counter and length is 1 greater than index
		newTH.innerHTML = '<div class="button" style="float:right" onclick="deleteRow('+(table.rows.length-2)+')">X</div><input id="rowHeader'+(table.rows.length-2)+'.name" name="rowHeader['+(table.rows.length-2)+'].name" type="text" type="text" value=""/></br><input id="rowHeader'+(table.rows.length-2)+'.weight" name="rowHeader['+(table.rows.length-2)+'].weight" type="text" type="text" value=""/>'
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
		}
	}
	
	function updateCells(){
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows.length; i++) {
			var rowHeader = table.rows[i].cells[0].getElementsByTagName("input")[0].value;
			for (var j=1; j<table.rows[i].cells.length; j++) {
				var columnHeader = table.rows[0].cells[j].getElementsByTagName("input")[0].value;
				var currCell = table.rows[i].cells[j].getElementsByTagName("textarea")[0];
				currCell.id="data'"+columnHeader+"''"+rowHeader+"'";
				currCell.name="data['"+columnHeader+"']['"+rowHeader+"']";
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
	    //$( "table").disableSelection();
	    $('table').dragtable({
			items: 'thead th:not( .notdraggable ):not( :has( .dragtable-drag-handle ) ), .dragtable-drag-handle',
			stop:function(e,ui){
				updateColumns();
			},
			cancel: 'input'
		});
	    

    });
    
	
</script>