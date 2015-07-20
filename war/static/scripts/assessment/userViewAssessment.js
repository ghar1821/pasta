function fillCell(cell, weight, data) {
	$(cell).empty();
	
	var span = document.createElement("span");
	var value = document.createTextNode(weight);
	span.appendChild(value);
	cell.appendChild(span);
	cell.appendChild(document.createElement("br"));
	
	var txtData = document.createTextNode(data);
	cell.appendChild(txtData);
	cell.appendChild(document.createElement("br"));
}

$(".showHide").each(function() {
	$(this).css("cursor", "pointer");
	var $content = $('#' + $(this).attr('showhide'));
	if($content.is($(".resultDetails").first())) {
		$(this).append("<span class='expanded-icon'> &laquo;</span>");
		setupTable($content.find("table.unitTestDetailsTable"));
	} else {
		$(this).append("<span class='expanded-icon'> &raquo;</span>");
		$content.hide();
	}
});

$(".showHide").on('click', function() {
	var $content = $('#' + $(this).attr('showhide'));
	if(!$content.has(".dataTables_wrapper").length) {
		setupTable($content.find("table.unitTestDetailsTable"));
	}
	$(this).find(".expanded-icon").html($content.css("display") === "none" ? " &laquo;" : " &raquo;");
	$content.slideToggle(300);
	$(".showHide").first().tipsy('hide');
});

$(".showHide").first().tipsy({
	gravity : 'e',
	fade : true,
	trigger: 'manual',
	title: function() {return "Click to toggle result details"}
});
$(".showHide").first().tipsy('show');
setTimeout(function() {
	$(".showHide").first().tipsy('hide');
}, 7000);

$('div.submitted-by-panel').on({
	mouseenter: function () {
		var $inner = $(this).children().first();
		$inner.stop();
		$inner.animate({ left: -$inner.children().last().width()}, "fast");
	},
	mouseleave: function () {
		var $inner = $(this).children().first();
		$inner.stop();
		$inner.animate({left: 0}, "fast");
	}
});
$('div.submitted-by-panel').each(function() {
	$(this).css("overflow", "hidden");
	var $inner = $(this).children().first();
	$(this).width($(this).width() - $inner.children().last().width());
	$inner.css("white-space", "nowrap");
	$inner.css("position", "relative");
});

function setupTable($table) {
	$table.addClass("stripe row-border hover");
	$table.DataTable({
		"searching" : false,
		"paging" : false,
		"info" : false,
		language : {
			emptyTable: "No unit test cases to display."
		}
	});
}

var updateComment = function($comments, $textarea) {
	$comments.html($textarea.wysiwyg("getContent"));
};

$(".modifyCommentsBtn").on("click", function() {
	var $commentDiv = $(this).prev();
	$commentDiv.find("textarea").each(function() {
		var timer;
		var $textarea = $(this);
		var $comments = $("#" + $textarea.attr("id").replace("modifyC", "c"));
		
		$textarea.wysiwyg({
			initialContent: function() {
				return "";
			},
			events: {
				keyup : function() {
					clearTimeout(timer);
					timer = setTimeout(function() {updateComment($comments, $textarea);}, 2000);
				},
				click : function() {
					updateComment($comments, $textarea);
				}
			},
			controls: {
				bold          : { visible : true },
				italic        : { visible : true },
				underline     : { visible : true },
				strikeThrough : { visible : true },
				
				justifyLeft   : { visible : true },
				justifyCenter : { visible : true },
				justifyRight  : { visible : true },
				justifyFull   : { visible : true },

				indent  : { visible : true },
				outdent : { visible : true },

				subscript   : { visible : true },
				superscript : { visible : true },
				
				undo : { visible : true },
				redo : { visible : true },
				
				insertOrderedList    : { visible : true },
				insertUnorderedList  : { visible : true },
				insertHorizontalRule : { visible : true },

				h4: {
					visible: true,
					className: 'h4',
					command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
					arguments: ($.browser.msie || $.browser.safari) ? '<h4>' : 'h4',
					tags: ['h4'],
					tooltip: 'Header 4'
				},
				h5: {
					visible: true,
					className: 'h5',
					command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
					arguments: ($.browser.msie || $.browser.safari) ? '<h5>' : 'h5',
					tags: ['h5'],
					tooltip: 'Header 5'
				},
				h6: {
					visible: true,
					className: 'h6',
					command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
					arguments: ($.browser.msie || $.browser.safari) ? '<h6>' : 'h6',
					tags: ['h6'],
					tooltip: 'Header 6'
				},
				cut   : { visible : true },
				copy  : { visible : true },
				paste : { visible : true },
				html  : { visible: true },
				increaseFontSize : { visible : true },
				decreaseFontSize : { visible : true }
			}
		});
	});
	$commentDiv.show();
	$(this).hide();
});
