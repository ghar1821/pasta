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
			
			var continueFunc = function() {
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
				
				if(container.is(".hbn-confirm")) {
					if(container[0].onclick) {
						container.data("oClickFunction", container[0].onclick);
						container[0].onclick = null;
					}
					var text = container.find(".hbn-text");
					container.on("click", function(e) {
						if($(this).data("confirm")) {
							var oFunc = container.data("oClickFunction");
							if(container.data("oClickFunction")) {
								oFunc.call(container[0], e);
							}
							return true;
						} else {
							$(this).data("confirm", true);
							text.data("oText", text.text());
							text.text("Sure?");
							var classes = "";
							if($(this).is(".flat")) {
								classes += "flat "
							}
							if($(this).is(".secondary")) {
								classes += "secondary "
							}
							$(this).data("oClasses", classes);
							$(this).removeClass(classes);
							e.preventDefault();
							return false;
						}
					});
					container.on("mouseleave", function() {
						$(this).data("confirm", false);
						text.text(text.data("oText"));
						$(this).addClass($(this).data("oClasses"));
					});
				}
			}
			
			// Wait until font awesome has added the "fa-events-icons-ready" class 
			// to the html element before checking sizes
			var contTimer = null;
			(function tryToContinue() {
				if(!$("html").is(".fa-events-icons-ready")) {
					contTimer = window.setTimeout(tryToContinue, 50);
				} else {
					window.clearTimeout(contTimer);
					contTimer = null;
					continueFunc();
				}
			})();
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