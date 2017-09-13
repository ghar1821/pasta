(function() {

	displayCallbacks["displayHistograms"] = function(content, allData) {
		
		if(!allData.assessments || allData.assessments.length == 0) {
			$("<div/>").addClass("part").text("No assessments to view.").appendTo(content);
			return;
		}
		
		var form = $("<div/>").addClass("pasta-form no-width part").appendTo(content);
		var row = $("<div/>").addClass("pf-horizontal two-col").appendTo(form);
		var selectDiv = $("<div/>").addClass("pf-item").appendTo(row);
		var sliderDiv = $("<div/>").addClass("pf-item hidden").appendTo(row);
		
		$("<div/>").addClass("pf-label").text("Assessment:").appendTo(selectDiv);
		var selectInputDiv = $("<div/>").addClass("pf-input").appendTo(selectDiv);
		
		var select = createAssessmentSelect(content, allData.assessments);
		select.appendTo(selectInputDiv);
		
		var sliderLabelDiv = $("<div/>").addClass("pf-label").text("Number of buckets: ").appendTo(sliderDiv);
		var sliderInputDiv = $("<div/>").addClass("pf-input").appendTo(sliderDiv);
		
		var slider = $("<input/>", {
			type: "range",
			min: 1,
			max: 10
		}).appendTo(sliderInputDiv);
		
		var sliderLabel = $("<span/>", {
			text: "Value",
		}).appendTo(sliderLabelDiv);
		
		var graph;
		
		function clearReport() {
			if(graph) {
				graph.empty();
			}
			if(chart) {
				chart.destroy();
				chart = undefined;
			}
		}
		
		function loadAssessment(assessment) {
			var numBuckets = Math.max(assessment.maxMark, assessment.numTests);
			
			if(!graph) {
				graph = $("<div/>").addClass("graph-container part").appendTo(content);
			}
			
			sliderDiv.toggleClass("hidden", assessment.maxMark <= 1);
			slider.attr("max", Math.max(1, numBuckets));
			slider.val(idealBuckets(numBuckets));
			
			slider.off("input");
			slider.on("input", function() {
				sliderLabel.text(slider.val());
				var loading = $("<div/>").addClass("loading").loading().appendTo(content);
				plotAssessmentMarks(assessment, slider.val(), graph);
				loading.remove();
			});
			
			clearReport();
			slider.trigger("input");
		}
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			clearReport();
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
	function plotAssessmentMarks(assessment, numBuckets, container) {
		var plotData = getPlotData(assessment.marks.slice(), numBuckets, assessment.maxMark, assessment.yourMark);
		if(assessment.classMarks) {
			plotData.classCounts = getPlotData(assessment.classMarks.slice(), numBuckets, assessment.maxMark).counts;
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
					return "Your mark";
				}
				return undefined;
			}
		};
		
		if(assessment.classMarks) {
			plotOptions = {
				column: {
					stacking: true
				}
			};
			tooltip = {
		        headerFormat: 'Mark: <b>{point.x}</b><br/>',
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
		        text: 'Assessment Marks for ' + assessment.assessment.name
		    },
			xAxis: {
				title: {
					text: "Total Mark"
				},
				categories: plotData.buckets,
			},
			yAxis: {
				title: {
					text: "Count"
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
	
	function getPlotData(data, numBuckets, max, yourMark) {
		data.sort(function(a, b) {
			return a - b
		});
		var highlightBucket;
		var step = Math.round((max / numBuckets) * 100) / 100;
		var buckets = ["N/A"];
		var counts = [0];
		var next = 0;
		
		// Count non-submissions
		while (data[0] < 0) {
			counts[0]++;
			data.shift();
		}
		if(yourMark !== undefined && yourMark < 0) {
			highlightBucket = buckets[0];
		}
	
		var newBucket;
		for (var i = 0; i < numBuckets; i++) {
			var start = next;
			if (i < numBuckets - 1) {
				next = Math.round((next + step) * 100) / 100;
				newBucket = "[" + start + "-" + next + ")";
				buckets.push(newBucket);
				if(yourMark !== undefined && highlightBucket === undefined && yourMark < next) {
					highlightBucket = newBucket;
				}
			} else {
				next = max;
				newBucket = "[" + start + "-" + next + "]";
				buckets.push(newBucket);
				if(yourMark !== undefined && highlightBucket === undefined && yourMark < next) {
					highlightBucket = newBucket;
				}
				counts.push(data.length);
				break;
			}
			var count = 0;
			while (data[0] < next) {
				count++;
				data.shift();
			}
			counts.push(count);
		}
	
		if(max == 0 && buckets.length == 2) {
			buckets[1] = "Submitted";
		}
		
		return {
			"buckets" : buckets,
			"counts" : counts,
			"highlightBucket" : highlightBucket
		};
	}
	
	function idealBuckets(num) {
		if(num <= 1) {
			return 1;
		}
		if(num <= 10) {
			return num;
		}
		var best = 1;
		for(var i = 2; i <= num; i++) {
			if(num % i == 0) {
				if(i < 10) {
					best = i;
				} else {
					return i;
				}
			}
		}
		return num;
	}

})();