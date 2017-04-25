/*
 * <div class='collapsible'>
 * 	<div class='head'>This is the header.</div>
 * 	<div>This is the content</div>
 * </div>
 * <script>
 * 	$(".collapsible").collapsible({"heading-selector":".head"});
 * </script>
 */

(function ( $ ) {
	
	$.fn.collapsible = function(options) {
		var selection = this;
		var settings = $.extend({}, $.fn.collapsible.defaults, options);
		
		selection.each(function(i, ele) {
			var base = $(ele);
			base.data("clb-settings", settings);
			
			var toggleBar = $("<div/>").addClass("clb-toggle-bar");
			var contentBar = $("<div/>").addClass("clb-content-wrapper");
			
			var div;
			var content;
			if(base.is("div")) {
				div = base;
			} else {
				div = $("<div/>");
				div = base.wrap(div).parent();
			}
			div.addClass("clb-parent");
			if(settings.collapsed) {
				div.addClass("collapsed");
			}
			
			toggleBar.css({
				"color": settings.style.color,
				"background-color": settings.style.background,
				"border": (settings.border ? "1px solid " + settings.style.hover.background : "none"),
				"border-radius": settings["border-radius"] + " 0 0 " + settings["border-radius"],
			});
			contentBar.css({
<<<<<<< Upstream, based on origin/master
				"border": (settings.border ? "1px solid " + settings.style.hover.background : "none"),
=======
				"border": (settings.border ? "1px solid " + settings.style.background : "none"),
>>>>>>> 85d877f Fixed 1px offset from collapsible toggle bar.
				"border-left-width": "0",
				"border-radius": "0 " + settings["border-radius"] + " " + settings["border-radius"] + " 0",
			});

			toggleBar.hover(function(e){
				$(this).css({
					"background-color": e.type === "mouseenter" ? settings.style.hover.background : settings.style.background,
					"color": e.type === "mouseenter" ? settings.style.hover.color : settings.style.color
				});
				if(settings.border) {
					$(this).add(contentBar).css({
						"border-color":e.type === "mouseenter"?settings.style.background:settings.style.hover.background
					});
				}
			});
			
			toggleBar.on("click", function() {
				var parent = $(this).closest(".clb-parent");
				var collapsed = parent.is(".collapsed");
				if(collapsed) {
					parent.expand();
				} else {
					parent.collapse();
				}
			});
			
			var content = $("<div/>").addClass('clb-content');
			content = div.wrapInner(content).children().first().textToSpan();
			contentBar = content.wrap(contentBar).parent();
			
			if(settings["heading-selector"]) {
				var onlyThisCollapsible = function(i, e) {
					return $(e).closest(".clb-content").is(content);
				}
				content.find(".clb-keep").removeClass("clb-keep");
				content.find(settings["heading-selector"])
					.filter(onlyThisCollapsible)
					.addClass("clb-keep");
				content.find(".clb-keep")
					.filter(onlyThisCollapsible)
					.each(function (i, e) {
						$(e).find("*")
							.add($(e).parents())
							.filter(onlyThisCollapsible)
							.addClass("clb-keep");
					});
			}
			
			div.prepend(toggleBar);
		});
		
		return this;
	}
	
	$.fn.collapsible.defaults = {
		"heading-selector": ".heading",
		border: true,
		collapsed: false,
		onExpand: undefined,
		onCollapse: undefined,
		onChange: undefined,
		style: {
			"border-radius": "10px",
			color: "#999",
			background: "lightgray",
			hover: {
				color: "lightgray",
				background: "cornflowerblue"
			},
		}
	}
	
	$.fn.expand = function() {
		if(this.is(".clb-parent")) {
			this.toggleClass("collapsed", false);
			var settings = this.data("clb-settings");
			if(settings.onChange) {
				settings.onChange.apply(this, ["expand"]);
			}
			if(settings.onExpand) {
				settings.onExpand.apply(this);
			}
		}
		return this;
	}
	
	$.fn.collapse = function() {
		if(this.is(".clb-parent")) {
			this.toggleClass("collapsed", true);
			var settings = this.data("clb-settings");
			if(settings.onChange) {
				settings.onChange.apply(this, ["collapse"]);
			}
			if(settings.onCollapse) {
				settings.onCollapse.apply(this);
			}
		}
		return this;
	}
	
	$.fn.textToSpan = function() {
		this.contents()
		    .filter(function() {
		        return this.nodeType === 3 && $.trim(this.nodeValue) !== '';
		    })
		    .wrap('<span/>');
		return this;
	}
	
}( jQuery ));