var changed = false;

function fillCell(cell, weight, selected, handMarkIndex, column, row, data) {
	$(cell).empty();
	cell.setAttribute("style", "cursor:pointer;");
	if(selected === "true") {
		$(cell).addClass("selectedMark");
	}
	
	var span = document.createElement("span");
	var value = document.createTextNode(weight);
	span.appendChild(value);
	cell.appendChild(span);
	cell.appendChild(document.createElement("br"));
	
	var txtData = document.createTextNode(data);
	cell.appendChild(txtData);
	cell.appendChild(document.createElement("br"));
	
	var radio = document.createElement("input");
	radio.setAttribute("name", "handMarkingResults[" + handMarkIndex + "].result[" + row + "]");
	radio.setAttribute("id", "handMarkingResults" + handMarkIndex + ".result" + row + column);
	radio.setAttribute("value", column);
	radio.setAttribute("type", "radio");
	radio.setAttribute("style", "display:none;");
	if(selected === "true") {
		$(radio).prop('checked', true)
	}
	cell.appendChild(radio);
	
	$(cell).on('click', function() {
		clickCell(this.cellIndex, this.parentNode.rowIndex, handMarkIndex);
	});
}

function clickAllInColumn(column, tableIndex){
	var table=document.getElementById("handMarkingTable"+tableIndex);
	for (var i=1; i<table.rows.length; i++) {
		clickCell(column, i, tableIndex);
	}
}

function clickCell(column, row, tableIndex){
	changed = true;
	var table=document.getElementById("handMarkingTable"+tableIndex);
	var currHeader = table.rows[row].cells[column].getElementsByTagName("input");
	if(currHeader.length != 0){
		currHeader[0].checked = true;
	}
	updateBackgrounds();
}

function updateBackgrounds() {
	$("tr.handMarkRow td").each(function() {
		if($(this).find("input:checked").length > 0) {
			$(this).addClass("selectedMark");
		} else {
			$(this).removeClass("selectedMark");
		}
	});
}

function registerEvents() {
	$(window).on('beforeunload', function() {
		if(changed){
	    	return "You have unsaved changes!";
		}
		if($('input[type=radio]:checked').size() < $("tr.handMarkRow").length){
			return "You have not completed marking."
		}
	});
	
	$(document).on('keydown', '#comments', function() {
		changed = true;
	});
	
	$(document).on('click', '#submit', function() {
		changed = false;
	});
}

(function($) {
	$(document).ready(function() {	
		$('#comments').wysiwyg({
			initialContent: function() {
				return "";
			},
		  controls: {
			bold          : { visible : true },
			italic        : { visible : true },
			underline     : { visible : true },
			strikeThrough : { visible : true },
			
			justifyLeft   : { visible : true },
			justifyCenter : { visible : true },
			justifyRight  : { visible : true },
			justifyFull   : { visible : true },

			indent  : { visible : true },
			outdent : { visible : true },

			subscript   : { visible : true },
			superscript : { visible : true },
			
			undo : { visible : true },
			redo : { visible : true },
			
			insertOrderedList    : { visible : true },
			insertUnorderedList  : { visible : true },
			insertHorizontalRule : { visible : true },

			h4: {
				visible: true,
				className: 'h4',
				command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
				arguments: ($.browser.msie || $.browser.safari) ? '<h4>' : 'h4',
				tags: ['h4'],
				tooltip: 'Header 4'
			},
			h5: {
				visible: true,
				className: 'h5',
				command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
				arguments: ($.browser.msie || $.browser.safari) ? '<h5>' : 'h5',
				tags: ['h5'],
				tooltip: 'Header 5'
			},
			h6: {
				visible: true,
				className: 'h6',
				command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
				arguments: ($.browser.msie || $.browser.safari) ? '<h6>' : 'h6',
				tags: ['h6'],
				tooltip: 'Header 6'
			},
			cut   : { visible : true },
			copy  : { visible : true },
			paste : { visible : true },
			html  : { visible: true },
			increaseFontSize : { visible : true },
			decreaseFontSize : { visible : true }
		  }
		});
	});
})(jQuery);