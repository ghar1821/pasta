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
		
		var tableDiv;
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			var loading = $("<div/>").addClass("loading").loading().appendTo(content);
			var assessment = $(this).find("option:selected").data("assessment");
			
			if(!tableDiv) {
				tableDiv = $("<div/>").addClass("table-container part").appendTo(content);
			}
			tableDiv.empty();
			
			var table = createTable(assessment);
			if(table) {
				tableDiv.append(table);
				table.DataTable({
					scrollX : true,
					iDisplayLength : 25,
					"columnDefs": [ {
						"type": "attempt",
						"targets" : "_all"
					}]
				});
			} else {
				tableDiv.append($("<span>").text("No unit tests."));
			}
			
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
	
	function createTable(summary) {
		if(!summary.studentResults) {
			return null;
		}
		
		var table = $("<table/>").addClass("display compact");
		
		var thead = $("<thead/>").addClass("rotate").appendTo(table);
		var headRow = $("<tr/>").appendTo(thead);
		
		$("<th/>").text("Username").appendTo(headRow);
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
			$.each(student.attempts, function(j, attempt) {
				$("<td/>").text(attempt).appendTo(bodyRow);
			});
		});
		
		var tfoot = $("<tfoot/>").appendTo(table);
		
		var meanRow = $("<tr/>").appendTo(tfoot);
		$("<th/>").text("Mean").appendTo(meanRow);
		$.each(summary.testMeans, function(i, mean) {
			$("<td/>").text(mean >= 0 ? round(mean, 2) : "").appendTo(meanRow);
		});
		
		var percentRow = $("<tr/>").appendTo(tfoot);
		$("<th/>").text("Completed (%)").appendTo(percentRow);
		$.each(summary.testPercentComplete, function(i, percent) {
			$("<td/>").text(percent >= 0 ? round(percent*100, 0) : "").appendTo(percentRow);
		});
		
		var meanCRow = $("<tr/>").appendTo(tfoot);
		$("<th/>").text("Mean Completed").appendTo(meanCRow);
		$.each(summary.testMeansCompleted, function(i, mean) {
			$("<td/>").text(mean >= 0 ? round(mean, 2) : "").appendTo(meanCRow);
		});
		
		return table;
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