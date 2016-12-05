function fillCell(cell, weight, index, dataId, data, error) {
	cell.removeClass("emptyCell");
	cell.addClass("cell");
	cell.empty();
	
	var row = cell.closest(".pf-horizontal").find(".header input[id$='id']").val();
	var rowCells = cell.closest(".pf-horizontal").find(".cell");
	var colIndex = rowCells.index(cell);
	var column = columnIdAt(colIndex);
	
	cell.append(
		$("<div/>")
			.addClass("pf-label")
			.html("Value:&nbsp")
			.append(
				$("<span/>")
					.addClass("cell-weight")
					.text(weight)
			).append(
				$("<div/>")
					.addClass("float-right")
					.append(
						$("<button/>")
							.addClass("flat delButton delButtonDelCell")
							.attr("type", "button")
							.text("Delete")
					).append(
						$("<button/>")
							.addClass("confButton confButtonDelCell")
							.attr("type", "button")
							.css("display", "none")
							.text("Confirm")
					)
			)
	);
	
	if(error && error.length) {
		$("<div/>")
			.attr("class", "pf-label")
			.appendTo(cell)
			.append(error);
	}
	
	cell.append(
		$("<div/>")
			.addClass("pf-input")
			.append(
				$("<textarea/>")
					.attr("name", "newData[" + index + "].data")
					.attr("id", "newData" + index + ".data")
					.append(data)
			)
	).append(
		$("<input/>")
			.attr("type", "hidden")
			.attr("name", "newData[" + index + "].id")
			.attr("id", "newData" + index + ".id")
			.val(dataId)
	).append(
		$("<input/>")
			.attr("type", "hidden")
			.attr("name", "newData[" + index + "].column")
			.attr("id", "newData" + index + ".column")
			.val(column)
	).append(
		$("<input/>")
			.attr("type", "hidden")
			.attr("name", "newData[" + index + "].row")
			.attr("id", "newData" + index + ".row")
			.val(row)
	)
}

function fillEmptyCell(cell) {
	cell.removeClass("emptyCell");
	cell.empty();
	
	$("<button/>")
		.attr("type", "button")
		.addClass("flat newCell float-right")
		.text("New Cell")
		.appendTo(cell);
}

function fillHeaderCell(cell, newIndex, upperType) {
	cell.empty();
	cell.addClass("header " + upperType.toLowerCase());
	
	$("<div/>")
		.addClass("pf-item")
		.append(
			$("<div/>")
				.addClass("pf-label")
				.text("Name")
				.append(
					$("<div/>")
						.addClass("pf-item float-right")
						.append(
							$("<button/>")
								.attr("type", "button")
								.addClass("flat delButton delButtonDel" + upperType)
								.text("Delete")
						)
						.append(
							$("<button/>")
								.attr("type", "button")
								.addClass("confButton confButtonDel" + upperType)
								.css("display", "none")
								.text("Confirm")
						)
				)
		)
		.append(
			$("<div/>")
				.addClass("pf-input")
				.append(
					$("<input/>")
						.attr("type", "text")
						.attr("name", "new" + upperType + "Header[" + newIndex + "].name")
						.attr("id", "new" + upperType + "Header" + newIndex + ".name")
						.val("New " + upperType)
				)
		)
		.appendTo(cell);
		
	$("<div/>")
		.addClass("pf-item")
		.append(
			$("<div/>")
				.addClass("pf-label")
				.text("Weight")
		)
		.append(
			$("<div/>")
				.addClass("pf-input")
				.append(
					$("<input/>")
						.attr("type", "text")
						.attr("name", "new" + upperType + "Header[" + newIndex + "].weight")
						.attr("id", "new" + upperType + "Header" + newIndex + ".weight")
						.val(0.0)
				)
		)
		.appendTo(cell);
	
	$("<input/>")
		.attr("type", "hidden")
		.attr("name", "new" + upperType + "Header[" + newIndex + "].id")
		.attr("id", "new" + upperType + "Header" + newIndex + ".id")
		.val(--newHeaderCount)
		.appendTo(cell);
	
}

function fillRowHeaderCell(cell, newIndex) {
	fillHeaderCell(cell, newIndex, "Row");
}

function fillColumnHeaderCell(cell, newIndex) {
	fillHeaderCell(cell, newIndex, "Column");
}

function registerEvents() {
	$(document).on('click', '.delButton', function() {
		$(this).toggle().next().toggle();
	});
	$(document).on('click', '.confButtonDelCell', function() {
		deleteCell($(this).closest(".cell"));
	});
	$(document).on('mouseout', '.confButton', function() {
		$(this).toggle().prev().toggle();
	});
	$(document).on('click', '.newCell', function() {
		newCell($(this).closest(".cell"));
	});
	$(document).on('click', '.confButtonDelRow', function() {
		deleteRow($(this).closest(".pf-horizontal"));
	});
	$(document).on('click', '.confButtonDelColumn', function() {
		var cells = $(this).closest(".pf-horizontal").find(".pf-item.header");
		var cell = $(this).closest(".pf-item.header");
		deleteColumn(cells.index(cell));
	});
	
	var delHighlight = "#ffd9d9";
	function hoverHighlight(selector, getCells) {
		$(document).on('mouseover', selector, function() {
			var cells = getCells($(this));
			cells.find("input, textarea").each(function(i, el){
				$(el).data("obck", $(el).css("background"))
					.css("background", delHighlight);
			});
		});
		$(document).on('mouseout', selector, function() {
			var cells = getCells($(this));
			cells.find("input, textarea").each(function(i, el){
				$(el).css("background", $(el).data("obck"));
			});
		});
	}
	hoverHighlight('.confButtonDelCell,.delButtonDelCell', function(button) {
		return button.closest(".cell");
	});
	hoverHighlight('.confButtonDelRow,.delButtonDelRow', function(button) {
		return button.closest(".pf-horizontal");
	});
	hoverHighlight('.confButtonDelColumn,.delButtonDelColumn', function(button) {
		var header = button.closest(".header");
		var colId = header.find("input[id$='id']").val();
		return $("#handMarkingTable input[id$='column'][value='" + colId + "']").closest(".cell").add(header);
	});

	$(document).on('blur', 'input', function() {
		updateColumns();
		updateRows();
		updateCells();
		
		var table = $("#handMarkingTable");
		var cols = table.find(".column.header");
		
		// sort columns
		for(var i = 0; i < cols.length; i++) {
			for(var j = i+1; j < cols.length; j++) {
				var col1Value = table.find(".column.header").eq(i).find("input[id$='weight']").val();
				var col2Value = table.find(".column.header").eq(j).find("input[id$='weight']").val();
				if(parseFloat(col1Value) > parseFloat(col2Value))	{
					swapColumn(i, j);
				}
			}
		}
	});
	$("form").on("submit", function() {
		var $table = $("#handMarkingTable");
		var $rowHeads = $(".header.row", $table);
		var total = 0;
		$rowHeads.each(function() {
			total += parseFloat($(this).find("input[id$='weight']").val());
		});
		if(Math.abs(total - 1.0) > 0.0001) {
			var confirmResult = confirm("As hand marking templates work with weights, your row weights will be scaled to be out of 1.0.\n\nClick 'Cancel' if this is not okay.");
			if(!confirmResult) {
				return false;
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
	
	var newIndex = $("#handMarkingTable .pf-horizontal:first .column.header").length;
	$("#handMarkingTable .pf-horizontal").each(function(i, row) {
		var cell = $("<div/>").addClass("pf-item compact").appendTo($(row));
		if(i == 0) {
			fillColumnHeaderCell(cell, newIndex);
		} else {
			cell.attr("id", "cell_" + columnIdAt(newIndex) + "_" + rowIdAt(i-1));
			newCell(cell);
		}
	});
	updateColumns();
}

function deleteColumn(columnIndex){
	var rows = $("#handMarkingTable .pf-horizontal");
	var complete = rows.length;
	var checkComplete = function(cell) {
		cell.remove();
		if(--complete <= 0) {
			updateColumns();
		}
	}
	rows.each(function(i, row) {
		if(i == 0) {
			var cell = $(row).find(".header").eq(columnIndex);
		} else {
			var cell = $(row).find(".cell").eq(columnIndex);
		}
		cell.animate({width:'toggle'}, "fast", function() {
			checkComplete(cell);
		});
	});
}

function currColClass() {
	var numCols = $("#handMarkingTable .header.column").length + 1;
	return numCols > 4 ? "n-col" : ["one","two","three","four"][numCols-1]+"-col";
}

function updateColumns(){
	$("#handMarkingTable .header.column").each(function(i, column) {
		var header = $(column);
		var fields = ["name", "weight", "id"];
		for(var j = 0; j < fields.length; j++) {
			var field = fields[j];
			header.find("input[id$='." + field + "']")
				.attr("id", "newColumnHeader"+i+"." + field)
				.attr("name", "newColumnHeader["+i+"]." + field)
		}
	});
	$("#handMarkingTable .pf-horizontal[class*='-col']")
		.removeClass("one-col two-col three-col four-col n-col")
		.addClass(currColClass());
}

function addRow(){
	var table=$("#handMarkingTable");
	var numCols = table.find(".pf-horizontal:first .column.header").length;
	var numRows = table.find(".pf-horizontal").length - 1;
	
	var newRow = $("<div/>").addClass("pf-horizontal " + currColClass());
	table.append(newRow);
	for(var i = 0; i <= numCols; i++) {
		var cell = $("<div/>").addClass("pf-item compact");
		newRow.append(cell);
		if(i == 0) {
			fillRowHeaderCell(cell, numRows);
		} else {
			cell.attr("id", "cell_" + columnIdAt(i-1) + "_" + rowIdAt(numRows));
			newCell(cell);
		}
	}
}

function deleteRow(row){
	row.slideUp("fast", function() {
		$(this).remove();
		updateRows();
	});
}

function updateRows(){
	$("#handMarkingTable .pf-horizontal").slice(1).each(function(i, row) {
		var header = $(row).find(".header");
		var fields = ["name", "weight", "id"];
		for(var j = 0; j < fields.length; j++) {
			var field = fields[j];
			header.find("input[id$='." + field + "']")
				.attr("id", "newRowHeader"+i+"." + field)
				.attr("name", "newRowHeader["+i+"]." + field)
		}
	});
}

function updateCells(){
	var table = $("#handMarkingTable");
	var colVals = table.find(".column.header input[id$='.weight']").map(function() {return $(this).val()});
	var rowVals = table.find(".row.header input[id$='.weight']").map(function() {return $(this).val()});
	
	table.find(".pf-horizontal").slice(1).each(function(rowIndex, row) {
		$(row).find(".cell").each(function(colIndex, cell) {
			var value = Number((rowVals[rowIndex] * colVals[colIndex]).toFixed(3));
			$(cell).find(".cell-weight").text(value);
		});
	});
}	

function deleteCell(cell){
	fillEmptyCell(cell);
}

function newCell(cell){
	fillCell(cell, 0.0, newDataPosition++, 0, "");
	updateCells();
}

$(function() {
	$( "#handMarkingTable" ).sortable({
        connectWith: "#handMarkingTable",
        dropOnEmpty: true,
        items: "> .pf-horizontal:not(:first)",
        stop: function(event, ui){
        	updateCells();
        }
    });
	
	$( ".emptyCell" ).each(function() {
        $(this).removeClass("emptyCell");
        fillEmptyCell($(this));
    });
	
	updateColumns();
});

function swapColumn(column1Index, column2Index){
	var headers = $("#handMarkingTable").find(".column.header");
	var head1 = headers.eq(column1Index).find("input");
	var head2 = headers.eq(column2Index).find("input");
	for(var i = 0; i < head1.length; i++) {
		var temp = head1.eq(i).val();
		head1.eq(i).val(head2.eq(i).val());
		head2.eq(i).val(temp);
	}
	
	var rows = $("#handMarkingTable").find(".pf-horizontal").slice(1);
	rows.each(function(i, row) {
		var cell1 = $(row).find(".cell").eq(column1Index);
		var cell2 = $(row).find(".cell").eq(column2Index);
		var anchor = cell1.prev();
		cell1.detach().insertBefore(cell2);
		cell2.detach().insertAfter(anchor);
	});
}