var changed = false;

function registerEvents() {
	$(document).on('keydown', '#comments', function() {
		changed = true;
	});

	$('#detailsToggle').on('click', function() {
		$("div.resultDetails").slideToggle('fast');
		$(this).text($(this).text() == "Show Details" ? "Hide Details" : "Show Details");
	});
}

(function($) {
	$(document).ready(function() {
		
		tinymce.init({
            selector: "#comments",
            plugins: "table code link textcolor",
            toolbar: "undo redo | styleselect | forecolor backcolor | bold italic | alignleft aligncenter alignright alignjustify | code-styles-split | bullist numlist outdent indent | link | code",
            setup: function(editor) {
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

		registerEvents();
		
		$("div.resultDetails").hide();
		$("table.unitTestDetailsTable").css("width", "100%");
		$("table.unitTestDetailsTable").DataTable({
			retrieve: true,
			"searching" : false,
			"paging" : false,
			"info" : false,
			language : {
				emptyTable: "No unit test cases to display."
			}
		});
	});
})(jQuery);

