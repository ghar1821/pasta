function fillCell(cell, weight, data) {
	$(cell).empty();
	
	var span = document.createElement("span");
	var value = document.createTextNode(weight);
	span.appendChild(value);
	cell.appendChild(span);
	cell.appendChild(document.createElement("br"));
	
	var txtData = document.createTextNode(data);
	cell.appendChild(txtData);
	cell.appendChild(document.createElement("br"));
}