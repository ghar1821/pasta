function fillCell(cell, weight, index, dataId, column, row, data, error) {
	$(cell).removeClass("emptyCell");
	$(cell).empty();
	
	var span = document.createElement("span");
	var value = document.createTextNode(weight);
	span.appendChild(value);
	cell.appendChild(span);
	if(error) {
		$(cell).append("&nbsp;");
		$(cell).append(error);
	}
	cell.appendChild(document.createElement("br"));
	
	var dataBox = document.createElement("textarea");
	dataBox.setAttribute("name", "newData[" + index + "].data");
	dataBox.setAttribute("id", "newData" + index + ".data");
	dataBox.setAttribute("style", "height:90%; width:95%");
	dataBox.appendChild(document.createTextNode(data));
	cell.appendChild(dataBox);
	
	var dataIdVal = document.createElement("input");
	dataIdVal.setAttribute("name", "newData[" + index + "].id");
	dataIdVal.setAttribute("id", "newData" + index + ".id");
	dataIdVal.setAttribute("value", dataId);
	dataIdVal.setAttribute("type", "hidden");
	cell.appendChild(dataIdVal);
	
	var colVal = document.createElement("input");
	colVal.setAttribute("name", "newData[" + index + "].column");
	colVal.setAttribute("id", "newData" + index + ".column");
	colVal.setAttribute("value", column);
	colVal.setAttribute("type", "hidden");
	cell.appendChild(colVal);
	
	var rowVal = document.createElement("input");
	rowVal.setAttribute("name", "newData[" + index + "].row");
	rowVal.setAttribute("id", "newData" + index + ".row");
	rowVal.setAttribute("value", row);
	rowVal.setAttribute("type", "hidden");
	cell.appendChild(rowVal);
	
	var delButton = document.createElement("div");
	delButton.setAttribute("class", "button delButton");
	delButton.setAttribute("style", "text-align: center;");
	delButton.appendChild(document.createTextNode("Delete Cell"));
	cell.appendChild(delButton);
	
	var confButton = document.createElement("div");
	confButton.setAttribute("class", "button confButton confButtonDelCell");
	confButton.setAttribute("style", "display:none; text-align: center;");
	confButton.appendChild(document.createTextNode("Confirm"));
	cell.appendChild(confButton);
}

function fillEmptyCell(cell) {
	$(cell).removeClass("emptyCell");
	$(cell).empty();
	
	var newButton = document.createElement("div");
	newButton.setAttribute("class", "button newCell");
	newButton.setAttribute("style", "text-align: center;");
	newButton.appendChild(document.createTextNode("New Cell"));
	cell.appendChild(newButton);
}

function fillHeaderCell(cell, newIndex, upperType) {
	$(cell).empty();
	
	var txtName = document.createElement("input");
	txtName.setAttribute("name", "new" + upperType + "Header[" + newIndex + "].name")
	txtName.setAttribute("id", "new" + upperType + "Header" + newIndex + ".name")
	txtName.setAttribute("type", "text");
	txtName.setAttribute("value", "New " + upperType);
	cell.appendChild(txtName);
	cell.appendChild(document.createElement("br"));
	
	var txtWeight = document.createElement("input");
	txtWeight.setAttribute("name", "new" + upperType + "Header[" + newIndex + "].weight")
	txtWeight.setAttribute("id", "new" + upperType + "Header" + newIndex + ".weight")
	txtWeight.setAttribute("type", "text");
	txtWeight.setAttribute("value", "0.0");
	cell.appendChild(txtWeight);
	
	var valId = document.createElement("input");
	valId.setAttribute("name", "new" + upperType + "Header[" + newIndex + "].id")
	valId.setAttribute("id", "new" + upperType + "Header" + newIndex + ".id")
	valId.setAttribute("type", "hidden");
	valId.setAttribute("value", --newHeaderCount);
	cell.appendChild(valId);
	
	var delButton = document.createElement("div");
	delButton.setAttribute("class", "button delButton");
	delButton.setAttribute("style", "text-align: center;");
	delButton.appendChild(document.createTextNode("Delete " + upperType));
	cell.appendChild(delButton);
	
	var confButton = document.createElement("div");
	confButton.setAttribute("class", "button confButton confButtonDel" + upperType);
	confButton.setAttribute("style", "display:none; text-align: center;");
	confButton.appendChild(document.createTextNode("Confirm"));
	cell.appendChild(confButton);
}

function fillRowHeaderCell(cell, newIndex) {
	fillHeaderCell(cell, newIndex, "Row");
}

function fillColumnHeaderCell(cell, newIndex) {
	fillHeaderCell(cell, newIndex, "Column");
}

function registerEvents() {
	$(document).on('click', '.delButton', function() {
		$(this).slideToggle("fast").next().slideToggle("fast");
	});
	$(document).on('click', '.confButtonDelCell', function() {
		deleteCell(this.parentNode.parentNode.rowIndex, this.parentNode.cellIndex);
	});
	$(document).on('mouseout', '.confButton', function() {
		$(this).slideToggle("fast").prev().slideToggle("fast");
	});
	$(document).on('click', '.newCell', function() {
		newCell(this.parentNode.parentNode.rowIndex-1, this.parentNode.cellIndex-1);
	});
	$(document).on('click', '.confButtonDelRow', function() {
		deleteRow(this.parentNode.parentNode.rowIndex);
		updateRows();
	});
	$(document).on('click', '.confButtonDelColumn', function() {
		deleteColumn(this.parentNode.cellIndex);
		updateColumns()
	});
	$(document).on('blur', 'input', function() {
		updateColumns();
		updateRows();
		updateCells();
		// sort columns
		var table=document.getElementById("handMarkingTable");
		for (var i=1; i<table.rows[0].cells.length; i++) {
			for (var j=i+1; j<table.rows[0].cells.length; j++) {
				var row1Value = table.rows[0].cells[i].getElementsByTagName("input")[1].value;
				var row2Value = table.rows[0].cells[j].getElementsByTagName("input")[1].value;
				if(parseFloat(row1Value) > parseFloat(row2Value))	{
					swapColumn(i, j);
				}
			}
		}
	});
}

function rowIdAt(index) {
	return document.getElementById("newRowHeader" + index + ".id").value;
}

function columnIdAt(index) {
	return document.getElementById("newColumnHeader" + index + ".id").value;
}

var newHeaderCount = 0;

function addColumn(){
	var table=document.getElementById("handMarkingTable");
	var tblHeadObj = table.tHead;
	var newIndex = table.rows[0].cells.length-1;
	for (var i=0; i<tblHeadObj.rows.length; i++) {
		var newTH = document.createElement('th');
		tblHeadObj.rows[i].appendChild(newTH);
		fillColumnHeaderCell(newTH, newIndex);
	}

	var tblBodyObj = document.getElementById("handMarkingTable").tBodies[0];
	for (var i=0; i<tblBodyObj.rows.length; i++) {
		var addedCell = tblBodyObj.rows[i].insertCell(-1);
		addedCell.setAttribute("id", "cell_" + columnIdAt(newIndex) + "_" + rowIdAt(i));
		newCell(i, newIndex);
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
		
		currHeader[0].id="newColumnHeader"+(i-1)+".name";
		currHeader[0].name="newColumnHeader["+(i-1)+"].name";
		
		currHeader[1].id="newColumnHeader"+(i-1)+".weight";
		currHeader[1].name="newColumnHeader["+(i-1)+"].weight";
		
		currHeader[2].id="newColumnHeader"+(i-1)+".id";
		currHeader[2].name="newColumnHeader["+(i-1)+"].id";
	}
}

function addRow(){
	var table=document.getElementById("handMarkingTable");
	table.insertRow(table.rows.length)
	
	// -2 since the top corner should not be counted and length is 1 greater than index
	var newIndex = table.rows.length-2;
	
	var newTH = document.createElement('th');
	table.rows[table.rows.length-1].appendChild(newTH);
	fillRowHeaderCell(newTH, newIndex);
	
	
	for (var i=1; i<table.rows[0].cells.length; i++) {
		var addedCell = table.rows[table.rows.length-1].insertCell(i);
		addedCell.setAttribute("id", "cell_" + columnIdAt(i-1) + "_" + rowIdAt(newIndex));
		newCell(newIndex, i-1);
	}
}

function deleteRow(row){
	document.getElementById("handMarkingTable").deleteRow(row);
}

function updateRows(){
	var table=document.getElementById("handMarkingTable");
	for (var i=1; i<table.rows.length; i++) {
		var currHeader = table.rows[i].cells[0].getElementsByTagName("input");
		
		currHeader[0].id="newRowHeader"+(i-1)+".name";
		currHeader[0].name="newRowHeader["+(i-1)+"].name";
		
		currHeader[1].id="newRowHeader"+(i-1)+".weight";
		currHeader[1].name="newRowHeader["+(i-1)+"].weight";
		
		currHeader[2].id="newRowHeader"+(i-1)+".id";
		currHeader[2].name="newRowHeader["+(i-1)+"].id";
	}
}

function updateCells(){
	var table=document.getElementById("handMarkingTable");
	for (var i=1; i<table.rows.length; i++) {
		var rowValue = table.rows[i].cells[0].getElementsByTagName("input")[1].value;
		for (var j=1; j<table.rows[i].cells.length; j++) {
			if(table.rows[i].cells[j].getElementsByTagName("textarea").length != 0){
				var columnValue = table.rows[0].cells[j].getElementsByTagName("input")[1].value;
				var currCellValue = table.rows[i].cells[j].getElementsByTagName("span")[0];
				currCellValue.innerHTML=parseFloat((rowValue*columnValue).toFixed(3));
			}
		}
	}
}	

function deleteCell(row, column){
	var table=document.getElementById("handMarkingTable");
	var cell = table.rows[row].cells[column];
	fillEmptyCell(cell);
}

function newCell(row, column){
	var table=document.getElementById("handMarkingTable");
	var cell = table.rows[row+1].cells[column+1];
	fillCell(cell, 0.0, newDataPosition++, 0, columnIdAt(column), rowIdAt(row), "");
	updateCells();
}

$(function() {
	$( "tbody.sortable" ).sortable({
        connectWith: "tbody",
        dropOnEmpty: true,
        
        stop: function(event, ui){
        	updateCells();
        }
    });
	
	$( "td.emptyCell" ).each(function() {
        $(this).removeClass("emptyCell");
        fillEmptyCell($(this).context);
    });
});

function swapColumn(column1Index, column2Index){
	var table=document.getElementById("handMarkingTable");
	
	var inputs1 = table.rows[0].cells[column1Index].getElementsByTagName("input");
	var inputs2 = table.rows[0].cells[column2Index].getElementsByTagName("input");
	for(var j=0; j<inputs1.length; j++) {
		var temp = inputs1[j].value;
		inputs1[j].value = inputs2[j].value;
		inputs2[j].value = temp;
	}

	$('#handMarkingTable tbody tr').each(function() {
	    var tr = $(this);
	    var td1 = tr.find('td:eq(' + (column1Index - 1) + ')');
	    var td2 = tr.find('td:eq(' + (column2Index - 1) + ')');
	    td1.detach().insertAfter(td2);
	    td1 = tr.find('td:eq(' + (column1Index - 1) + ')');
	    td2.detach().insertBefore(tr.find('td:eq(' + (column1Index - 1) + ')'));
	});
}