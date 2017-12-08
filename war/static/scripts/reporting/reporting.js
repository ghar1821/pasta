var displayCallbacks = {};

(function() {
	$(".report").collapsible({
		collapsed: true, 
		"heading-selector": ".section-title",
		onExpand: function() {
			var report = $(this);
			if(!report.is(".loaded")) {
				loadReport(report);
			}
		}
	});
	
	Highcharts.getOptions().colors[0] = $(".tab-bar").css("background-color");
})();

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
			container.addClass("loaded");
			content.empty();
			var error = data['error'];
			if(error) {
				$("<div/>").addClass("part").text(error).appendTo(content);
				return;
			}
			var callback = displayCallbacks[data['callback']];
			if(typeof callback === 'function') {
				callback(content, data);
			}
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
	
	var controls = $("<div/>")
		.addClass("controls part")
		.appendTo(popup);
	
	var messages = $("<div/>")
		.addClass("messages")
		.appendTo(controls);
	
	var buttons = $("<div/>")
		.addClass("button-panel")
		.appendTo(controls);
	
	$.ajax({
		headers : {
			'Accept' : 'application/json',
		},
		url : "./permissions/" + id + "/",
		data : {},
		type : "GET",
		error : function(error) {
			console.error(error);
			$("<div/>")
				.addClass("error")
				.text("Could not load report permissions.")
				.appendTo(messages)
				.fadeOut(3000, function() {$(this).remove();});
		},
		success : function(data) {
			if(data['error']) {
				$("<div/>")
					.addClass("error")
					.text("Error loading permissions: " + data['error'])
					.appendTo(messages)
					.fadeOut(3000, function() {$(this).remove();});
			} else {
				populatePermissions(permissions, data);
			}
		}
	});
	
	$("<button/>")
		.addClass("save-permissions")
		.text("Save")
		.on("click", function() {
			savePermissions(id, messages);
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

	bPopup.reposition(0);
});

function populatePermissions(container, data) {
	$("<h3/>").text("Default Permissions:").appendTo(container);
	$("<p/>").addClass("info").text("These permissions apply to all assessments not overridden by specific rules (below).").appendTo(container);
	
	var defHeader = $("<div/>").addClass("header row").appendTo(container);
	$("<span/>").addClass("name").appendTo(defHeader);
	$.each(data["valid-permissions"], function(i, perm) {
		$("<span/>").addClass("perm").text(perm.description).appendTo(defHeader);
	});
	
	var defRow = $("<div/>").addClass("perms row default").appendTo(container);
	$("<span/>").addClass("name").appendTo(defRow);
	var defaults = data["default-permissions"];
	$.each(data["valid-permissions"], function(i, perm) {
		var cell = $("<span/>").addClass("perm").appendTo(defRow);
		var cellId = "default-" + perm.name;
		var check = $("<input/>").attr("type", "checkbox")
			.addClass("custom-check default")
			.attr("name", cellId)
			.attr("data-key", cellId)
			.prop("checked", defaults[perm.name])
			.appendTo(cell);
		$("<label/>").attr("data-key", cellId).appendTo(cell);
	});
	
	$("<h3/>").text("Assessment-specific Permissions:").appendTo(container);
	$("<p/>").addClass("info").text("These permissions override the default permissions above. If grey, the assessment will defer to the default value. If red or green, the chosen permission will override the default value. Ctrl/Cmd click the toggle to toggle deferring to the default.").appendTo(container);
	
	var assHeader = $("<div/>").addClass("header row").appendTo(container);
	$("<span/>").addClass("name").appendTo(assHeader);
	$.each(data["valid-permissions"], function(i, perm) {
		$("<span/>").addClass("perm").text(perm.description).appendTo(assHeader);
	});
	
	var assContainer = $("<div/>").addClass("permission-scroll").appendTo(container);
	$.each(data["assessment-permissions"], function(c, category) {
		var categoryName = category.category;
		if(categoryName) {
			$("<div/>").addClass("category row").text(categoryName).appendTo(assContainer);
		}
		$.each(category.assessments, function(a, assessment) {
			var assRow = $("<div/>").addClass("perms row").data("ass", assessment.id).appendTo(assContainer);
			if(assessment.duplicate) {
				assRow.addClass("duplicate");
			}
			$("<span/>").addClass("name").text(assessment.name).appendTo(assRow);
			var permissions = assessment.permissions;
			$.each(data["valid-permissions"], function(i, perm) {
				var cell = $("<span/>").addClass("perm").appendTo(assRow);
				var cellId = assessment.id + "-" + perm.name;
				var check = $("<input/>").attr("type", "checkbox")
					.addClass("custom-check")
					.attr("name", cellId)
					.attr("data-key", cellId)
					.appendTo(cell);
				var hasVal = (permissions[perm.name] !== undefined);
				if(hasVal) {
					check.prop("checked", permissions[perm.name]);
				} else {
					check.prop("indeterminate", true);
				}
				$("<label/>").attr("data-key", cellId).appendTo(cell);
			});
		});
	});
}

$(document).on("click", ".perm label", function(e) {
	e.preventDefault();
	var check = $(this).closest(".perm").find("input[type='checkbox']");
	var checks = $(".perm input[type='checkbox'][data-key='" + $(this).data("key") + "']");
	if(!check.is(".default") && (e.ctrlKey || e.metaKey)) {
		var toggle = check.is(":indeterminate");
		checks.prop("indeterminate", !toggle);
	} else {
		checks.trigger("click");
	}
});

function savePermissions(id, messages) {
	
	var permissions = {};
	$(".perms.row:not(.duplicate) .perm").each(function() {
		var check = $(this).find("input[type='checkbox']");
		var key = check.data("key");
		if(check.prop("indeterminate")) {
			permissions[key] = null;
		} else {
			permissions[key] = check.is(":checked");
		}
	});
	
	$.ajax({
		headers : {
			'Accept' : 'application/json',
		},
		url : "./savePermissions/" + id + "/",
		data: JSON.stringify(permissions),
		type: "POST",
		dataType: 'json',
		contentType: 'application/json',
		error : function(error) {
			console.error(error);
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
				.fadeOut(1000, function() {
					$(this).remove(); 
					$(".popup").bPopup().close();
				});
		}
	});
}