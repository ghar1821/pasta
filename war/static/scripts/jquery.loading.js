(function ( $ ) {
	 
	$.fn.loading = function(options) {
		var settings = $.extend({}, $.fn.loading.defaults, options);
		this.each(function(i, container) {
			container = $(container);
			var loader = $("<div/>")
				.addClass("loader")
				.css({
					height: settings.height, 
					width: settings.width
				})
				.appendTo(container);
			$("<div/>").addClass("inner pasta-loader one").appendTo(loader);
			$("<div/>").addClass("inner pasta-loader two").appendTo(loader);
			$("<div/>").addClass("inner pasta-loader three").appendTo(loader);
		});
		return this;
	}
	
	$.fn.loading.defaults = {
		width: '32px',
		height: '32px',
	}
	
}( jQuery ));