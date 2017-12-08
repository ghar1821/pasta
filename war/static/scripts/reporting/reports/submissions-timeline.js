(function() {

	displayCallbacks["displaySubmissions"] = function(content, data) {
		
		if(!data.categories || data.categories.length == 0) {
			$("<div/>").addClass("part").text("No assessments to view.").appendTo(content);
			return;
		}
		
		var form = $("<div/>").addClass("pasta-form no-width part").appendTo(content);
		var selectDiv = $("<div/>").addClass("pf-item").appendTo(form);
		
		$("<div/>").addClass("pf-label").text("Assessment:").appendTo(selectDiv);
		var selectInputDiv = $("<div/>").addClass("pf-input").appendTo(selectDiv);
		
		var select = createAssessmentSelect(content, data.categories);
		select.appendTo(selectInputDiv);
		
		var graphDiv;
		var notStartedDiv;
		
		function clearReport() {
			if(graphDiv) {
				graphDiv.empty();
			}
			if(notStartedDiv) {
				notStartedDiv.empty();
			}
			if(chart) {
				chart.destroy();
				chart = undefined;
			}
		}
		
		function loadAssessment(assessment) {
			if(!graphDiv) {
				graphDiv = $("<div/>").addClass("graph-container part").appendTo(content);
				notStartedDiv = $("<div/>").addClass("part").appendTo(content);
			}
			clearReport();
			plotSubmissions(assessment, graphDiv);
			showNotStarted(assessment, notStartedDiv);
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
					url : "./" + data.reportId + "/" + assessment.assessment.id + "/",
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
	
	function createAssessmentSelect(container, categories) {
		var select = $("<select/>");
		$("<option/>").appendTo(select);
		$.each(categories, function(j, category) {
			var group = select;
			if(category) {
				group = $("<optgroup/>", {
					label: category.category
				})
				.appendTo(select);
			}
			$.each(category.assessments, function(i, assessment) {
				$("<option/>", {
					text: assessment.assessment.name,
					value: assessment.assessment.id
				})
				.data("assessment", assessment)
				.appendTo(group);
			});
		});
		return select;
	}
	
	var chart;
	function plotSubmissions(data, container) {
		var dates = parseDates(data.dates, "DD/MM/YYYY");
		
		chart = Highcharts.chart(container[0], {
			title: {
		        text: 'Submissions for ' + data.assessment.name
		    },
		    tooltip: {
		    	shared: true
		    },
		    xAxis: {
		    	type: 'datetime',
		    },
		    yAxis: [{
		    	title: {
		    		text: "Daily submission count",
		    		style: {
		                color: Highcharts.getOptions().colors[0]
		            }
		    	},
		    	labels: {
		            style: {
		                color: Highcharts.getOptions().colors[0]
		            }
		        },
		    	allowDecimals: false
		    },{
		    	title: {
		    		text: "Number of students with submission(s)",
		    		style: {
		                color: Highcharts.getOptions().colors[1]
		            }
		    	},
		    	labels: {
		            style: {
		                color: Highcharts.getOptions().colors[1]
		            }
		        },
		    	opposite: true,
		    	allowDecimals: false,
		    	min: 0,
		    	max: data.studentCount
		    }],
		    series: [{
		    	name: "Daily submission count",
		    	type: 'spline',
		    	data: zip(dates, data.submissionCounts),
		    	yAxis: 0,
		    	marker: {
		    		enabled: false
		    	}
		    }, {
		    	name: "Number of students with submission(s)",
		    	type: 'spline',
		    	data: zip(dates, data.startedCounts),
		    	yAxis: 1,
		    	tooltip: {
		    		pointFormatter: function() {
		    			var perc = round(this.y / data.studentCount * 100, 0);
		    			return '<span style="color:' + this.color + '">\u25CF</span> ' 
		    				+ this.series.name + ': <b>' + this.y + '</b> (' + perc + '%)<br/>';
		    		}
		    	},
		    	marker: {
		    		enabled: false
		    	}
		    }]
		});
	}
	
	function showNotStarted(assessment, container) {
		var notStarted = assessment.noSubmission;
		if(!notStarted || notStarted.length == 0) {
			container.append($("<p/>").text("Everyone has made at least one submission."));
			return;
		}
		var count = notStarted.length;
		container.append($("<p/>").css("font-weight", "bold").text(count + " student" + (count == 1 ? " has" : "s have") + " not yet made any submissions for this assessment."));
		$(" <a/>").text("Click here to show students with no submission.").on("click", function() {
			$(this).remove();
			showNotStartedList(notStarted, container);
		}).appendTo(container);
	}
	
	function showNotStartedList(notStarted, container) {
		notStarted.sort();
		var notStartedDiv = $("<div/>").addClass("not-started-container").appendTo(container);
		$.each(notStarted, function(i, username) {
			$("<div/>")
				.addClass("not-started")
				.append($("<a/>").attr("href", "../student/" + username + "/home/").text(username))
				.appendTo(notStartedDiv);
		});
	}
	
	function parseDates(dates, format) {
		return dates.map(function(date) {
			return moment.utc(date, format).valueOf();
		});
	}
	function formatPercentages(values, max) {
		return values.map(function(value) {
			if(max <= 0) return 0;
			return round((value / max) * 100, 1);
		});
	}
	function zip(a, b) {
		return a.map(function (e, i) {
			return [e, b[i]];
		});
	}
	
	function round(x, dp) {
		return Number(Number(x).toFixed(dp));
	}
	
})();