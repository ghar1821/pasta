var fillArray = [];
var changed = false;

function fillCell($cell, weight, data) {
	$cell.empty();
	var $span = $("<span class='hm-data-weight'>" + weight + "</span>");
	$cell.append($span);
	$cell.append($("<br/>"));
	$cell.append(data);
}

$(function() {
	$(fillArray).each(function(index, fillFunction) {
		fillFunction();
	});
});
