(function() {

	displayCallbacks["displayTestHistograms"] = function(content, allData) {
		
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
		
		if(assessment.numTests == 0) {
			$("<div/>").addClass("part").text("No unit tests in this assessment.").appendTo(container);
			return;
		}
		
		var plotData = getPlotData(assessment.passCounts.slice(), assessment.numTests, assessment.yourPassCount);
		if(assessment.classPassCounts) {
			plotData.classCounts = getPlotData(assessment.classPassCounts.slice(), assessment.numTests).counts;
		}
		
		var plotOptions = {};
		var tooltip = {};
		var legend = {
			enabled: false	
		};
		var series;
		var columnLabels = {
			enabled: true,
			formatter: function() {
				var match = this.axis ? plotData.buckets[this.x] : this.x;
				if(match === plotData.highlightBucket) {
					return "Your submission";
				}
				return undefined;
			}
		};
		
		if(assessment.classPassCounts) {
			plotOptions = {
				column: {
					stacking: true
				}
			};
			tooltip = {
		        headerFormat: 'Tests passed: <b>{point.x}</b><br/>',
		        pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}',
		    };
			legend = {
				enabled: true	
			};
			series = [{
				name: "Other class submissions",
				data: plotData.counts,
				pointPadding: 0,
		        groupPadding: 0,
			},{
				name: "Your class submissions",
				data: plotData.classCounts,
				pointPadding: 0,
		        groupPadding: 0,
			}];
		} else {
			var highlightIndex = plotData.buckets.indexOf(plotData.highlightBucket);
			series = [{
				name: "Submissions",
				data: plotData.counts.map(function(point, i) {
					if(i == highlightIndex) {
						return {
							y: point,
							color: Colours.brighter(Highcharts.getOptions().colors[0], 0.4),
						};
					}
					return point;
				}),
				pointPadding: 0,
		        groupPadding: 0,
		        dataLabels: columnLabels
			}];
		}
		
		chart = Highcharts.chart(container[0], {
			chart: {
				type: 'column'
			},
			title: {
		        text: 'Passing Test Cases for ' + assessment.assessment.name
		    },
			xAxis: {
				title: {
					text: "Test Cases Passed"
				},
				categories: plotData.buckets,
			},
			yAxis: {
				title: {
					text: "Student Count"
				},
				allowDecimals: false,
				stackLabels: columnLabels
			},
			plotOptions: plotOptions,
			legend: legend,
			tooltip: tooltip,
			series: series,
		});
	}
	
	function getPlotData(data, max, yourPassCount) {
		var buckets = ["N/A"];
		var counts = [0];
		for(var i = 0; i <= max; i++) {
			buckets.push(String(i));
			counts.push(0);
		}
		
		var highlightBucket;
		if(yourPassCount !== undefined) {
			if(yourPassCount < 0) {
				highlightBucket = buckets[0];
			} else {
				highlightBucket = buckets[yourPassCount+1];
			}
		}
		
		for(i = 0; i < data.length; i++) {
			if(data[i] < 0) {
				counts[0]++;
			} else {
				counts[data[i]+1]++;
			}
		}
		
		return {
			"buckets" : buckets,
			"counts" : counts,
			"highlightBucket" : highlightBucket
		};
	}
})();