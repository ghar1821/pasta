(function() {

	displayCallbacks["displaySubmissions"] = function(content, data) {
		
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
		
		var graphDiv;
		var notStartedDiv;
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			var loading = $("<div/>").addClass("loading").loading().appendTo(content);
			var assessment = $(this).find("option:selected").data("assessment");
			
			if(!graphDiv) {
				graphDiv = $("<div/>").addClass("graph-container part").appendTo(content);
				notStartedDiv = $("<div/>").addClass("part").appendTo(content);
			}
			graphDiv.empty();
			notStartedDiv.empty();
			plotSubmissions(assessment, graphDiv);
			showNotStarted(assessment, notStartedDiv);
			loading.remove();
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
	
	function plotSubmissions(data, container) {
		var dates = parseDates(data.dates, "DD/MM/YYYY");
		
		var graph = Highcharts.chart(container[0], {
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
		    		text: "Submission Count",
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
		    		text: "Students Started",
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
		    	name: "Student Submissions",
		    	type: 'spline',
		    	data: zip(dates, data.submissionCounts),
		    	yAxis: 0,
		    	marker: {
		    		enabled: false
		    	}
		    }, {
		    	name: "Students Started",
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
		container.append($("<p/>").css("font-weight", "bold").text("The following students have not yet made any submissions for this assessment:"));
		if(!notStarted || notStarted.length == 0) {
			container.append($("<p/>").text("Everyone has made at least one submission."));
			return;
		}
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