(function() {

	displayCallbacks["displaySubmissions"] = function(content, data) {
		var form = $("<div/>").addClass("pasta-form no-width part").appendTo(content);
		var selectDiv = $("<div/>").addClass("pf-item").appendTo(form);
		
		$("<div/>").addClass("pf-label").text("Assessment:").appendTo(selectDiv);
		var selectInputDiv = $("<div/>").addClass("pf-input").appendTo(selectDiv);
		
		var select = createAssessmentSelect(content, data.assessments);
		select.appendTo(selectInputDiv);
		
		var graphDiv = $("<div/>").addClass("graph-container part").appendTo(content);
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			var loading = $("<div/>").addClass("loading").loading().appendTo(content);
			var assessment = $(this).find("option:selected").data("assessment");
			graphDiv.empty();
			plotSubmissions(assessment, graphDiv);
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
		            format: '{value}%',
		            style: {
		                color: Highcharts.getOptions().colors[1]
		            }
		        },
		    	opposite: true,
		    	allowDecimals: false,
		    	min: 0,
		    	max: 100
		    }],
		    series: [{
		    	name: "Student Submissions",
		    	type: 'spline',
		    	data: zip(dates, data.submissionCounts),
		    	yAxis: 0,
		    }, {
		    	name: "Students Started",
		    	type: 'spline',
		    	data: zip(dates, formatPercentages(data.startedCounts, data.studentCount)),
		    	yAxis: 1,
		    	tooltip: {
		    		valueSuffix: "%"
		    	}
		    }]
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