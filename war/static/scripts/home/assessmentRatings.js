function setDots($dotDiv, rating) {
	$dotDiv.children("div").each(function() {
		$(this).toggleClass('empty', $(this).attr('rating') > rating)
		$(this).toggleClass('full', $(this).attr('rating') <= rating)
	});
}

function resetDots($dotDiv) {
	var $ratingInput = $dotDiv.children("input");
	var rating = $ratingInput.val();
	rating = Math.max(0, Math.min(rating, 5));
	setDots($dotDiv, rating);
}

function askForRating(hide) {
	var $first = $(".ratingControls").has("#rating[value=0]").first();
	if(hide) {
		$first.tipsy('hide');
	} else {
		$first.tipsy({
			gravity : 'e',
			fade : true,
			trigger: 'manual',
			fallback: "How difficult did you find this assessment?",
			offset: 10,
			css: {width: 40}
		});
		$first.tipsy('show');
		$first.on("mouseover", function() {$(this).tipsy('hide');});
	}
}

function sendRating($form) {
	var assessmentId = $form.attr("assessment");
	var $allForms = $("[class^='ratingForm'][assessment='" + assessmentId + "']")
	
	$.ajax({
		headers : {
			'Accept' : 'application/json',
		},
		url : $form.attr("action"),
		data : $form.serialize(),
		type : "POST",
		statusCode : {
			500 : function() {
				alert("Failed to send rating. Please try again later.");
			}
		},
		success : function() {
			var $confirm = $form.find("#confirmRating");
			$confirm.show();
			$confirm.html("<span style='color:green;'>&#10004; Thanks!</span>");
			$confirm.fadeOut(2000, function() {
				$allForms.closest(".rate-assessment").hide();
			});
		}
	});
}

$(document).ready(function() {
	$("[class^='ratingForm']").each(function() {
		var $form = $(this);
		var $thisDotDiv = $form.find(".ratingControls").children(".ratingDots");
		
		var assessmentId = $form.attr("assessment");
		var $allForms = $("[class^='ratingForm'][assessment='" + assessmentId + "']")
		var $dotDivs = $allForms.find(".ratingControls").children(".ratingDots");
		
		var $ratingInput = $thisDotDiv.children("input");
		for(var i = 1; i <= 5; i++) {
			var $newDiv = 
				jQuery('<div/>', {
				    class: "ratingDot empty r" + i,
				    rating: i
				});
			$newDiv.on('click', function() {
				$ratingInput.val($(this).attr('rating'));
				sendRating($form);
			});
			$newDiv.on('mouseover', function() {
				setDots($dotDivs, $(this).attr('rating'));
			});
			$newDiv.on('mouseout', function() {
				resetDots($dotDivs);
			});
			$thisDotDiv.append($newDiv);
		}
		resetDots($thisDotDiv);
	});
	
	$(".ratingForm").on('submit', function() {
		var $form = $(this);
		sendRating($form);
		return false;
	});
	
	$(".showComments").on('click', function(e) {
		e.preventDefault();
		$('#extraComments' + $(this).attr('assessment')).bPopup();
	});
	
	$(".closeRating").on('click', function(e) {
		e.preventDefault();
		var assessmentId = $(this).attr("assessment");
		var allForms = $("[class^='ratingForm'][assessment='" + assessmentId + "']")
		allForms.closest(".assessment-box").find(".rate-assessment").hide();
	});
	
	$(".openRating").on('click', function(e) {
		e.preventDefault();
		var assessmentId = $(this).attr("assessment");
		var allForms = $("[class^='ratingForm'][assessment='" + assessmentId + "']")
		allForms.closest(".assessment-box").find(".rate-assessment").show();
	});
	$(".rate-assessment").hide();
	$(".openRating[data-autoclick='true']").trigger("click");
	
	$(".ratingSubmit").on('click', function() {
		var assessment = $(this).attr('assessment');
		$form = $('.ratingForm' + assessment).first();
		var $popup = $(this).closest('#extraComments' + assessment);
		var $comment = $popup.find(".ratingComment");
		$form.children('#comment').val($comment.val());
		$popup.bPopup().close();
		sendRating($form);
	});
	
	askForRating();
});