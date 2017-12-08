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
	setupTable($content.find("table.unitTestDetailsTable"));
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
		var viewContentWidth = Math.max($inner.children().first().width(), $inner.children().last().width() + 5);
		$inner.animate({ left: -viewContentWidth}, "fast");
	},
	mouseleave: function () {
		var $inner = $(this).children().first();
		$inner.stop();
		$inner.animate({left: 0}, "fast");
	}
});
$('div.submitted-by-panel').each(function() {
	$(this).css("margin-top", "4px");
	$(this).css("overflow", "hidden");
	var $inner = $(this).children().first();
	$(this).width($(this).width() - $inner.children().last().width());
	$inner.css("white-space", "nowrap");
	$inner.css("position", "relative");
});

function setupTable($table) {
	$table.toggleClass("stripe row-border hover", true);
	$table.DataTable({
		retrieve: true,
		"searching" : false,
		"paging" : false,
		"info" : false,
		language : {
			emptyTable: "No unit test cases to display."
		}
	});
}

var updateComment = function($comments) {
	$comments.html(tinymce.activeEditor.getContent());
};

$(".modifyCommentsBtn").on("click", function() {
	var $commentDiv = $(this).prev();
	$commentDiv.find("textarea").each(function() {
		var timer;
		var $textarea = $(this);
		var $comments = $("#" + $textarea.attr("id").replace("modifyC", "c"));
		
		tinymce.init({
            selector: "#" + $textarea.attr("id"),
            plugins: "table code link textcolor",
            toolbar: "undo redo | styleselect | forecolor backcolor | bold italic | alignleft aligncenter alignright alignjustify | code-styles-split | bullist numlist outdent indent | link | code",
            setup: function(editor) {
            	editor.on('keyup', function() {
            		clearTimeout(timer);
					timer = setTimeout(function() {updateComment($comments);}, 800);
                });
                editor.on('change', function() {
                	updateComment($comments);
                });
                editor.addButton('code-styles-split', {
                    type: 'splitbutton',
                    text: 'code',
                    title: 'Toggle <code> tags',
                    icon: false,
                    onclick: function() {
                    	editor.execCommand('mceToggleFormat', false, 'code');
                    },
                    menu: [
                        {text: 'Inline <code>', onclick: function() {
                        	editor.execCommand('mceToggleFormat', false, 'code');
                        }},
                        {text: 'Block <pre>', onclick: function() {
                        	editor.execCommand('mceToggleFormat', false, 'pre');
                        }}
                    ]
                });
            },
            style_formats_merge: true,
        });
	});
	$commentDiv.show();
	$(this).hide();
});

$(".hbn-button").hoverButton({
	dataKey: "hbn-icon"
});
