function setStars($starDiv, rating) {
	$starDiv.children("div").each(function() {
		$(this).toggleClass('emptyStar', $(this).attr('rating') > rating)
		$(this).toggleClass('fullStar', $(this).attr('rating') <= rating)
	});
}

function resetStars($starDiv) {
	var $ratingInput = $starDiv.children("input");
	var rating = $ratingInput.val();
	rating = Math.max(0, Math.min(rating, 5));
	setStars($starDiv, rating)
}

function updateTooHard($tooHardDiv) {
	$tooHardDiv.toggleClass('selected', $tooHardDiv.children('.ratingTooHard').is(':checked'));
}

function sendRating($form) {
	var $tooHard = $form.find(".tooHardDiv").children(".ratingTooHard");
	$form.children('#tooHard').val($tooHard.is(':checked'));
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
			$confirm.html("<span style='color:green;'>&#10004;</span>");
			$confirm.fadeOut(2000);
		}
	});
}

$(document).ready(function() {
	$("[class^='ratingForm']").each(function() {
		var $form = $(this);
		var $starDiv = $form.children(".ratingControls").children(".ratingStars");
		var $ratingInput = $starDiv.children("input");
		for(var i = 1; i <= 5; i++) {
			var $newDiv = 
				jQuery('<div/>', {
				    class: "emptyStar",
				    rating: i
				});
			$newDiv.on('click', function() {
				$ratingInput.val($(this).attr('rating'));
				sendRating($form);
			});
			$newDiv.on('mouseover', function() {
				setStars($starDiv, $(this).attr('rating'));
			});
			$newDiv.on('mouseout', function() {
				resetStars($starDiv);
			});
			$starDiv.append($newDiv);
		}
		resetStars($starDiv);
	});
	
	$(".ratingForm").on('submit', function() {
		var $form = $(this);
		sendRating($form);
		return false;
	});
	
	$(".showComments").on('click', function() {
		$('#extraComments' + $(this).attr('assessment')).bPopup();
	});
	
	$(".ratingSubmit").on('click', function() {
		var assessment = $(this).attr('assessment');
		$form = $('.ratingForm' + assessment);
		var $comment = $('#extraComments' + assessment).find(".ratingComment");
		$form.children('#comment').val($comment.val());
		$('#extraComments' + assessment).bPopup().close();
		sendRating($form);
	});
	
	$(".tooHardDiv").on('click', function() {
		$checkbox = $(this).children('.ratingTooHard');
		$checkbox.prop('checked', !$checkbox.prop('checked'));
		updateTooHard($(this));
		sendRating($(this).parents("#ratingForm"));
	});
});