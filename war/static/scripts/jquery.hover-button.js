(function ( $ ) {
	 
	$.fn.hoverButton = function(options) {
		if(typeof options === "string") {
			options = {icon: options};
		}
		
		var settings = $.extend({}, $.fn.hoverButton.defaults, options);
		this.each(function(i, container) {
			container = $(container);
			if(!container.is("button") && 
					!container.is("input[type=button]") && 
					!container.is("input[type=submit]")) {
				console.warn("Calling hoverButton on something other than a button will be ignored. Target: ", container[0]);
				return true;
			}
			if(container.is(".hbn")) {
				return true;
			}
			container.addClass("hbn");
			
			var oContentContainer = $("<span>").addClass("hbn-text");
			container.wrapInner(oContentContainer);
			oContentContainer = container.children(".hbn-text").detach();
			
			var content = $("<span>").addClass("hbn-content").appendTo(container);
			
			var icon = settings.icon;
			if(settings.dataKey) {
				icon = container.data(settings.dataKey);
			}
			
			$("<span>")
				.addClass("fa")
				.addClass(icon)
				.addClass(settings.modifiers)
				.attr("aria-hidden", "true")
				.prependTo(content);
			content.data("minsize", content.width());
			
			oContentContainer.appendTo(content);
			content.data("maxsize", content.width());
			
			if(settings.round) {
				container.css("border-radius", container.outerHeight() / 2);
			}
			
			function wide() {
				content.css("width", content.data("maxsize"));
			}
			function narrow() {
				content.css("width", content.data("minsize"));
			}
			
			if(settings.expandEvents) {
				narrow();
				container.on("mouseenter focus", wide);
				container.on("mouseleave blur", narrow);
			} else {
				wide();
			}
		});
		return this;
	}
	
	$.fn.hoverButton.defaults = {
		icon: 'fa-info',
		modifiers: 'fa-lg',
		dataKey: null,
		round: true,
		expandEvents: true,
	}
	
}( jQuery ));