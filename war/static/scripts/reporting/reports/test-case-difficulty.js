(function() {

	displayCallbacks["displayTestDifficulty"] = function(content, allData) {
		
		if(!allData.assessments || allData.assessments.length == 0) {
			$("<div/>").addClass("part").text("No assessments to view.").appendTo(content);
			return;
		}
		
		var form = $("<div/>").addClass("pasta-form no-width part").appendTo(content);
		var selectDiv = $("<div/>").addClass("pf-item").appendTo(form);
		
		$("<div/>").addClass("pf-label").text("Assessment:").appendTo(selectDiv);
		var selectInputDiv = $("<div/>").addClass("pf-input").appendTo(selectDiv);
		
		var select = createAssessmentSelect(content, allData.assessments);
		select.appendTo(selectInputDiv);
		
		var graph;
		
		function loadAssessment(assessment) {
			if(!graph) {
				graph = $("<div/>").addClass("graph-container part").appendTo(content);
			}
			graph.empty();
			plotAssessmentPassCounts(assessment, graph);
		}
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			var loading = $("<div/>").addClass("loading").loading().appendTo(content);
			var assessment = $(this).find("option:selected").data("assessment");
			
			if(assessment.loaded) {
				loadAssessment(assessment);
				loading.remove();
			} else {
				$.ajax({
					headers : {
						'Accept' : 'application/json',
					},
					url : "./" + allData.reportId + "/" + assessment.assessment.id + "/",
					data : {},
					type : "GET",
					success: function(response) {
						$.extend(true, assessment, response);
						assessment.loaded = true;
						loadAssessment(assessment);
					},
					error: function(error) {
						console.log("error", error);
						content.empty();
						$("<span/>").text("Error loading report. ").appendTo(content);
						$("<a/>").text("Try again.").on("click", function() {
							select.trigger("change");
						}).appendTo(content);
					},
					complete: function() { loading.remove(); }
				})
			}
		});
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
	
	var chart;
	function plotAssessmentPassCounts(assessment, container) {
		if(chart) {
			chart.destroy();
			chart = undefined;
		}
		
		if(assessment.tests.length == 0) {
			$("<div/>").addClass("part").text("No unit tests in this assessment.").appendTo(container);
			return;
		}
		
		var hasClass = assessment.testResults[0].classCounts !== undefined;
		var series = [{
            name: (hasClass ? "Other Class - " : "") + 'Pass',
            data: assessment.testResults.map(function(tr) { return tr.counts.pass; }),
            color: "#0C0"
        }];
		if(hasClass) {
			series.push({
	            name: 'Your Class - Pass',
	            data: assessment.testResults.map(function(tr) { return tr.classCounts.pass; }),
	            color: "#080"
	        });
		}
		series.push({
			name: (hasClass ? "Other Class - " : "") + 'Fail/Error',
			data: assessment.testResults.map(function(tr) { return -tr.counts.fail - tr.counts.error; }),
			color: "red"
		});
		if(hasClass) {
			series.push({
				name: 'Your Class - Fail/Error',
	            data: assessment.testResults.map(function(tr) { return -tr.classCounts.fail - tr.classCounts.error; }),
	            color: "#800"
			});
		}
		
		var categories = assessment.tests;
		chart = Highcharts.chart(container[0], {
	        chart: {
	            type: 'bar'
	        },
	        title: {
	            text: 'Current Unit Test Results for ' + assessment.assessment.name
	        },
	        xAxis: [{
	            categories: categories,
	            reversed: true,
	            labels: {
	                step: 1
	            }
	        }, {
	            opposite: true,
	            reversed: true,
	            categories: categories,
	            linkedTo: 0,
	            labels: {
	                step: 1
	            }
	        }],
	        yAxis: {
	        	allowDecimals: false,
	            title: {
	                text: null
	            },
	            labels: {
	                formatter: function () {
	                    return Math.abs(this.value);
	                }
	            }
	        },
	        plotOptions: {
	            series: {
	                stacking: 'normal'
	            }
	        },
	        tooltip: {
	            formatter: function () {
	                return '<b>' + this.series.name + ' ' + this.point.category + '</b><br/>' +
	                    'Submission count: ' + Highcharts.numberFormat(Math.abs(this.point.y), 0);
	            }
	        },
	        series: series
	    });
	}
})();