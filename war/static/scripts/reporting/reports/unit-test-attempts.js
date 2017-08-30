(function() {

	displayCallbacks["displayUnitTestAttempts"] = function(content, data) {
		
		if(!data.assessments || data.assessments.length == 0) {
			$("<div/>").addClass("part").text("No assessments to view.").appendTo(content);
			return;
		}
		
		var form = $("<div/>").addClass("pasta-form no-width part").appendTo(content);
		var selectDiv = $("<div/>").addClass("pf-item").appendTo(form);
		
		$("<div/>").addClass("pf-label").text("Assessment:").appendTo(selectDiv);
		var selectInputDiv = $("<div/>").addClass("pf-input").appendTo(selectDiv);
		
		var select = createAssessmentSelect(content, data.assessments);
		select.appendTo(selectInputDiv);
		
		content.append(explanation());
		
		var controls;
		var tableDiv;
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			var loading = $("<div/>").addClass("loading").loading().appendTo(content);
			var assessment = $(this).find("option:selected").data("assessment");
			
			if(assessment.summaries) {
				if(!controls) {
					controls = $("<div/>").addClass("part").appendTo(content);
				}
				updateControlsDiv(controls, false);
			}
			
			if(!tableDiv) {
				tableDiv = $("<div/>").addClass("table-container part").appendTo(content);
			}
			var studentTable = createStudentTable(assessment);
			tableDiv.data("studentTable", studentTable);
			
			if(assessment.summaries) {
				var classTable = createClassTable(assessment);
				tableDiv.data("classTable", classTable);
			}
			
			showTable(tableDiv, "studentTable", assessment.studentResults && assessment.studentResults.length == 1);
			loading.remove();
		});
	}
	
	$.fn.dataTable.ext.type.order['attempt-pre'] = function (data) {
		if(data === "-") {
			return 1000;
		}
		if(data === "") {
			return 1001;
		}
		return data;
	}
	
	function createAssessmentSelect(container, assessments) {
		var select = $("<select/>");
		$("<option/>").appendTo(select);
		$.each(assessments, function(i, assessment) {
			$("<option/>", {
				text: assessment.assessment.name,
				value: assessment.assessment.id
			})
			.data("assessment", assessment)
			.appendTo(select);
		});
		return select;
	}
	
	function explanation() {
		var explanation = $("<div/>").addClass("part");
		$("<p/>").text("A '-' indicates that they have attempted but not yet passed the test, and blank rows mean that the student has not yet attempted the test.").appendTo(explanation);
		$("<p/>").html("<strong>Mean</strong> is the average number of attempts that students have taken to complete the test, where students who have taken <code>n</code> attempts without passing the test will count as <code>n+1</code> attempts.").appendTo(explanation);
		$("<p/>").html("<strong>Mean completed</strong> is the average number of attempts for students that have successfully passed the test.").appendTo(explanation);
		return explanation;
	};
	
	var noTableFeatures = {
		info: false,
		lengthChange: false,
		searching: false,
		ordering: false,
		paging: false,
	};
	
	function createStudentTable(summary) {
		if(!summary.studentResults) {
			return null;
		}
		
		var table = $("<table/>").addClass("display compact");
		
		var thead = $("<thead/>").addClass("rotate").appendTo(table);
		var headRow = $("<tr/>").appendTo(thead);
		
		$("<th/>").text("Username").appendTo(headRow);
		$("<th/>").text("Stream").appendTo(headRow);
		$("<th/>").text("Class").appendTo(headRow);
		
		$.each(summary.testNames, function(i, name) {
			var span = $("<span/>")
				.data("oText", name)
				.text(name);
			shorten(span, 15);
			span.tipsy({
				gravity : 'e',
				title : function(){return $(this).data("oText");},
				offset: -(span.text().length * 4)
			});
			span.on('mouseover', function() {
				lengthen(span);
			});
			span.on('mouseout', function() {
				shorten(span, 15);
			});
			
			$("<th/>").append($("<div/>").append(span)).appendTo(headRow);
		});
		
		var tbody = $("<tbody/>").appendTo(table);
		$.each(summary.studentResults, function(i, student) {
			var bodyRow = $("<tr/>").appendTo(tbody);
			$("<td/>").text(student.username).appendTo(bodyRow);
			$("<td/>").text(student.stream).appendTo(bodyRow);
			$("<td/>").text(student['class']).appendTo(bodyRow);
			$.each(student.attempts, function(j, attempt) {
				$("<td/>").text(attempt).appendTo(bodyRow);
			});
		});
	
		var tfoot = $("<tfoot/>").appendTo(table);
		
		var meanRow = $("<tr/>").appendTo(tfoot);
		$("<th/>").attr("colspan", 3).text("Mean").appendTo(meanRow);
		$.each(summary.mainSummary.testMeans, function(i, mean) {
			$("<td/>").text(mean >= 0 ? round(mean, 2) : "").appendTo(meanRow);
		});
		
		var percentRow = $("<tr/>").appendTo(tfoot);
		$("<th/>").attr("colspan", 3).text("Completed (%)").appendTo(percentRow);
		$.each(summary.mainSummary.testPercentComplete, function(i, percent) {
			$("<td/>").text(percent >= 0 ? round(percent*100, 0) : "").appendTo(percentRow);
		});
		
		var meanCRow = $("<tr/>").appendTo(tfoot);
		$("<th/>").attr("colspan", 3).text("Mean Completed").appendTo(meanCRow);
		$.each(summary.mainSummary.testMeansCompleted, function(i, mean) {
			$("<td/>").text(mean >= 0 ? round(mean, 2) : "").appendTo(meanCRow);
		});
		
		return table;
	}
	
	function createClassTable(summary) {
		var table = $("<table/>").addClass("display compact class-table");
		
		var thead = $("<thead/>").addClass("rotate").appendTo(table);
		var headRow = $("<tr/>").appendTo(thead);
		$("<th/>").text("Stream").appendTo(headRow);
		$("<th/>").text("Class").appendTo(headRow);
		
		$.each(summary.testNames, function(i, name) {
			var span = $("<span/>")
				.data("oText", name)
				.text(name);
			shorten(span, 15);
			span.tipsy({
				gravity : 'e',
				title : function(){return $(this).data("oText");},
				offset: -(span.text().length * 4)
			});
			span.on('mouseover', function() {
				lengthen(span);
			});
			span.on('mouseout', function() {
				shorten(span, 15);
			});
			
			$("<th/>").append($("<div/>").append(span)).appendTo(headRow);
		});
		
		var tbody = $("<tbody/>").appendTo(table);
		$.each([summary.mainSummary].concat(summary.summaries), function(i, summ) {
			var bodyRow = $("<tr/>").appendTo(tbody);
			$("<td/>").text(summ.stream).appendTo(bodyRow);
			$("<td/>").text(summ['class']).appendTo(bodyRow);
			
			$.each(summary.testNames, function(j) {
				var m = summ.testMeans[j];
				var pc = summ.testPercentComplete[j];
				var mc = summ.testMeansCompleted[j];
				$("<td/>")
					.text('-')
					.addClass("summary-cell")
					.data("mean", m >= 0 ? round(m, 2) : "")
					.data("percent-complete", pc >= 0 ? round(pc*100, 0) : "")
					.data("mean-complete", mc >= 0 ? round(mc, 2) : "")
					.appendTo(bodyRow);
			});
		});
		
		return table;
	}
	
	function showTable(container, tableId, isStudent) {
		container.empty();
		
		var table = container.data(tableId);
		
		if(table) {
			table = table.clone(true);
			container.append(table);
			var options = {
				scrollX : true,
				iDisplayLength : 25,
				"columnDefs": [ {
					"targets" : [0, 1, 2],
					"type": "string"
				}, {
					"type": "attempt",
					"targets" : "_all",
					"searchable" : false,
				}]
			};
			
			var isClassTable = tableId == "classTable";
			if(isClassTable) {
				options.ordering = true;
				options["order"] = [[ 1, "asc" ],[ 0, "asc" ]];
				options.columnDefs[0].targets = [0, 1];
			}
			if(isStudent || isClassTable) {
				options = $.extend({}, noTableFeatures, options);
			}
			table.DataTable(options);
			
			updateSummaryCells("mean");
		} else {
			container.append($("<span>").text("No unit tests."));
		}
	}
	
	function updateControlsDiv(container, isClassTable) {
		container.empty();
		
		var text = isClassTable ?
				"You are viewing <strong>whole class</strong> statistics. To see individual student statistics, " :
				"You are viewing <strong>individual student</strong> statistics. To see whole class statistics, "
		
		$("<p/>")
				.append($("<span/>").html(text))
				.append($("<a/>").text("click here.").on("click", function() {
					updateControlsDiv(container, !isClassTable);
					showTable($(".table-container"), isClassTable ? "studentTable" : "classTable")
				}))
				.appendTo(container);
		
		if(isClassTable) {
			var options = $("<p/>").append($("<strong/>").text("Choose summary statistic:")).appendTo(container);
			var options = $("<div/>").addClass("statistics-options").appendTo(container);
			$.each([{label: "Mean", data: "mean", selected: true}, 
			        {label: "Percent complete (%)", data: "percent-complete", selected: false}, 
			        {label: "Mean (completed)", data: "mean-complete", selected: false}], function(i, option){
				$("<span/>")
					.addClass("option")
					.append($("<input/>", {
						type: "radio",
						name: "stat-choose",
						id: "sc-" + option.data,
						checked: option.selected
					}).on("click", function() {
						updateSummaryCells(option.data);
					}))
					.append($("<label/>", {
						"for": "sc-" + option.data,
						text: " " + option.label
					}))
					.appendTo(options);
			});
		}
	}
	
	function updateSummaryCells(dataName) {
		var table = $(".class-table").DataTable();
		$(".summary-cell").each(function() {
			var cell = table.cell(this);
			cell.data($(this).data(dataName));
		});
	}
	
	function round(x, dp) {
		return Number(Number(x).toFixed(dp));
	}
	
	function shorten(span, numChars) {
		if(span.text().length > numChars) {
			span.text(span.text().substr(0, numChars) + "...");
		}
	}
	function lengthen(span) {
		span.text(span.data("oText"));
	}
	
})();