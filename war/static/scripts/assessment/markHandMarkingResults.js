$(function() {
	$("th.hm-col-header.marking").each(function() {
		$(this).css("cursor", "pointer");
		$(this).on("click", function() {
			var colIndex = $(this).index();
			$(this).closest("table")
			.find("tr td:nth-child(" + (colIndex+1) + ")")
			.trigger("click");
		});
	});

	$("td.hm-cell.marking:not(.empty)").on("click", function() {
		changed = true;
		$(this).find("input:radio").prop("checked", true);
		updateBackgrounds();
	});

	$(window).on('beforeunload', function() {
		if(changed){
	    	return "You have unsaved changes!";
		}
		if($('input[type=radio]:checked').size() < $("tr.hm-row").length){
			return "You have not completed marking."
		}
	});

	$(document).on('submit', 'form', function() {
		changed = false;
	});
	
	updateBackgrounds();
});

function updateBackgrounds() {
	$("tr.hm-row td").each(function() {
		$(this).toggleClass("selectedMark", $(this).find("input:radio").is(":checked"));
	});
}

function fillMarkingCell($cell, weight, data, selected, resultIndex, columnId, rowId) {
	$cell.empty();
	$cell.removeClass("empty");
	$cell.css("cursor", "pointer");

	var $span = $("<span class='hm-data-weight'>" + weight + "</span>");
	$cell.append($span);
	$cell.append($("<br/>"));
	$cell.append(data);
	$cell.append($("<br/>"));
	
	var $radio = $("<input/>", {
		type: "radio",
		name: "handMarkingResults[" + resultIndex + "].result[" + rowId + "]",
		id: "handMarkingResults" + resultIndex + ".result" + rowId + columnId,
		value: columnId
	});
	$radio.hide();
	if(selected) {
		$radio.prop('checked', true)
	}
	$cell.append($radio);
}
