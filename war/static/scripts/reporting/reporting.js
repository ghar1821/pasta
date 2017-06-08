var displayCallbacks = {};

$(document).on("click", ".load-report", function() {
	var container = $(this).closest(".report");
	loadReport(container);
});

function loadReport(container) {
	var content = container.find(".report-content");
	var id = content.data("report");
	
	$("<div/>").addClass("loading").loading().appendTo(content);
	
	$.ajax({
		headers : {
			'Accept' : 'application/json',
		},
		url : "./" + id + "/",
		data : {},
		type : "GET",
		error : function(error) {
			console.log("error", error);
			content.empty();
			$("<span/>").text("Error loading report. ").appendTo(content);
			$("<a/>").text("Try again.").addClass("load-report").appendTo(content);
		},
		success : function(data) {
			content.empty();
			var error = data['error'];
			if(error) {
				content.text(error);
				return;
			}
			var callback = displayCallbacks[data['callback']];
			if(typeof callback === 'function') {
				callback(content, data);
			}
		},
		complete : function() {
			container.find("button.load-report").remove();
		}
	});
}

$(".edit-permissions").on("click", function() {
	var link = $(this);
	var container = link.closest(".report");
	var id = container.find(".report-content").data("report");
	var name = container.find(".report-name").text();
	
	var popup = $("<div/>").addClass("popup report-permissions");
	
	$("<h3>")
		.addClass("section-title")
		.text("Report Visibility - " + name)
		.appendTo(popup);
	
	var permissions = $("<div/>")
		.addClass("permissions part")
		.appendTo(popup);
	
	var selectedPermissions = [];
	container.find(".permission").each(function(i, item) {
		selectedPermissions.push($(item).text());
	});
	
	$.each(allPermissions, function(i, permission) {
		var makeSelected = function(container, selected) {
			container.toggleClass("selected", selected);
			container.children(".permission-icon").toggleClass("fa-check", selected);
			container.children(".permission-icon").toggleClass("fa-times", !selected);
		};
		
		var permDiv = $("<div/>")
			.addClass("permission")
			.data("perm-value", permission.value)
			.appendTo(permissions);
		$("<span>")
			.addClass("permission-icon fa fa-times")
			.appendTo(permDiv);
		$("<span>")
			.addClass("permission-text")
			.text(permission.text)
			.appendTo(permDiv);
		permDiv.on("click", function() {
			var selected = !$(this).is(".selected");
			makeSelected($(this), selected);
		});
		makeSelected(permDiv, selectedPermissions.indexOf(permission.text) >= 0);
	});
	
	var controls = $("<div/>")
		.addClass("controls part")
		.appendTo(popup);
	
	var messages = $("<div/>")
		.addClass("messages")
		.appendTo(controls);
	
	var buttons = $("<div/>")
		.addClass("button-panel")
		.appendTo(controls);
	
	$("<button/>")
		.addClass("save-permissions")
		.text("Save")
		.on("click", function() {
			selectedPermissions = [];
			var selectedPermissionTexts = [];
			permissions.find(".selected").each(function(i, e) {
				selectedPermissions.push($(e).data("perm-value"));
				selectedPermissionTexts.push($(e).find(".permission-text").text());
			});
			savePermissions(id, selectedPermissions, messages, container.find(".edit-permissions"), selectedPermissionTexts);
		})
		.appendTo(buttons);
	
	var closeButton = $("<button/>")
		.addClass("flat")
		.text("Cancel")
		.appendTo(buttons);
	
	var bPopup = popup.bPopup({
		"onClose": function() {popup.remove();}
	});
	closeButton.on("click", function() {
		bPopup.close();
	});
});

function savePermissions(id, selectedPermissions, messages, oPermissions, selectedPermissionTexts) {
	$.ajax({
		headers : {
			'Accept' : 'application/json',
		},
		url : "./savePermissions/" + id + "/",
		data : {permissions: selectedPermissions},
		type : "POST",
		error : function(error) {
			console.log("error", error);
			$("<div/>")
				.addClass("error")
				.text("Error saving permissions.")
				.appendTo(messages)
				.fadeOut(3000, function() {$(this).remove();});
		},
		success : function(data) {
			$("<div/>")
				.addClass("success")
				.text("Successfully saved permissions.")
				.appendTo(messages)
				.fadeOut(3000, function() {$(this).remove();});
			buildPermissions(oPermissions, selectedPermissionTexts)
		}
	});
}

function buildPermissions(permissions, selectedPermissions) {
	permissions.empty();
	if(selectedPermissions.length) {
		$.each(selectedPermissions, function(i, perm) {
			$("<span/>").addClass("permission").text(perm).appendTo(permissions);
		});
	} else {
		$("<span/>").addClass("permission").text("Nobody").appendTo(permissions);
	}
}