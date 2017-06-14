(function() {

	displayCallbacks["displayRatings"] = function(content, data) {
		
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
		
		var ratingsDiv;
		
		select.chosen({
			width: '100%'
		}).on("change", function(){
			var loading = $("<div/>").addClass("loading").loading().appendTo(content);
			var assessment = $(this).find("option:selected").data("assessment");
			
			if(!ratingsDiv) {
				ratingsDiv = $("<div/>").addClass("ratings-container").appendTo(content);
			}
			ratingsDiv.empty();
			
			ratingsDiv.append(createRatingsDiv(assessment));
			ratingsDiv.append(createCommentsDiv(assessment.comments));
			
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
	
	function createRatingsDiv(assessment) {
		var ratings = assessment.ratings;
		
		var div = $("<div/>").addClass("ratings part no-line");
		$("<h4/>").addClass("part-title").text("Ratings").appendTo(div);
		
		var ratingTable = $("<table/>").addClass("display compact").appendTo(div);
		ratingTable.append(
				$("<thead/>").append(
						$("<tr/>")
							.append($("<th/>").text("Rating"))
							.append($("<th/>").text("Count"))
				)
		);
		
		var tbody = $("<tbody/>").appendTo(ratingTable);
		$.each(group(ratings), function(rating, count) {
			tbody.append($("<tr/>")
						.append($("<td/>").addClass("center").text(rating))
						.append($("<td/>").addClass("center").text(count))
			);
		});
		
		var summaryTable = $("<table/>").addClass("display compact").appendTo(div);
		summaryTable.append(
				$("<thead/>").append(
						$("<tr/>")
							.append($("<th/>").text(""))
							.append($("<th/>").text(""))
				)
		);
		tbody = $("<tbody/>").appendTo(summaryTable);
		tbody.append($("<tr/>").append($("<th/>").text("Rating count")).append($("<td/>").text(assessment.ratingCount)));
		tbody.append($("<tr/>").append($("<th/>").text("Rating mean")).append($("<td/>").text(mean(ratings))));
		tbody.append($("<tr/>").append($("<th/>").text("Rating std. dev.")).append($("<td/>").text(stddev(ratings))));
		
		ratingTable.add(summaryTable).DataTable({
			width: '300px',
			info: false,
			lengthChange: false,
			searching: false,
			ordering: false,
			paging: false
		});
		
		return div;
	}
	
	function mean(ratings) {
		if(ratings.length <= 0) {
			return "";
		}
		var sum = 0;
		for(var i = 0; i < ratings.length; i++) {
			sum += ratings[i];
		}
		return round(sum / ratings.length, 2);
	}
	
	function stddev(ratings) {
		if(ratings.length <= 0) {
			return "";
		}
		var mu = mean(ratings);
		var sum = 0;
		var diffs = ratings.map(function(x) {
			return (mu-x)*(mu-x);
		});
		var sd = Math.sqrt(mean(diffs));
		return round(sd, 2);
	}
	
	function group(ratings) {
		var ratingsObj = {};
		for(var n = 1; n <= 5; n++) {
			ratingsObj[n] = 0;
		}
		for(var i = 0; i < ratings.length; i++) {
			var x = ratings[i];
			if(ratingsObj[x] === undefined) {
				ratingsObj[x] = 0;
			}
			ratingsObj[x]++;
		}
		return ratingsObj;
	}
	
	function createCommentsDiv(comments) {
		var div = $("<div/>").addClass("part");
		$("<h4/>").addClass("part-title").text("Comments").appendTo(div);
		
		if(comments.length == 0) {
			$("<p/>").text("No comments yet.").appendTo(div);
			return div;
		}
		
		$.each(comments, function(i, comment) {
			var commentDiv = $("<div>").addClass("comment").appendTo(div);
			var text = comment.split(/\r?\n/);
			$.each(text, function(j, part) {
				if(part) {
					$("<span/>").text(part).appendTo(commentDiv);
				}
				$("<br/>").appendTo(commentDiv);
			});
		});
		
		return div;
	}
	
	function round(x, dp) {
		return Number(Number(x).toFixed(dp));
	}
	
})();