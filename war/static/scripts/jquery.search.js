(function ( $ ) {
	 
	$.fn.searchable = function() {
		return this.each(function() {
			$(this).toggleClass("searchable", true);
		});
	};

	$.fn.searchNode = function() {
		return this.each(function() {
			$(this).toggleClass("search-node", true);
		});
	};

	$.fn.searchBox = function(options) {
		var settings = $.extend({
			placeholder : "Search..."
		}, options);
		return this.filter("input[type='text']").each(function() {
			$(this).wrap("<div class='search-box'></div>")
			$(this).attr("placeholder", settings.placeholder);
			$(this).on("keyup", function() {
				$.search($(this).val(), options);
			});
		});
	};
	
	$.search = function(text, options) {
		var settings = $.extend({
			anyTerm : false,
			resetOnBlank : true,
			beforeSearch : function() {},
			afterSearch : function() {},
			onReset : function() {}
		}, options);
		settings.beforeSearch();
		var terms = text.split(/\s+/);
		var changed = $(document).find(".search-node").each(function() {
			$(this).removeClass("parent-found");
			$(this).removeClass("child-found");
			$(this).removeClass("found");
			$(this).removeClass("not-found");
			if(settings.resetOnBlank && !text) {
				return $(this);
			}
			var found = !settings.anyTerm;
			for(var i = 0; i < terms.length && found != settings.anyTerm; i++) {
				if(!terms[i]) {
					continue;
				}
				var node = $(this);
				var numFound = node.find(".searchable").filter(function(index, element) {
					return ($(element).closest(".search-node").is(node));
				}).filter(":Contains('" + terms[i] + "')").length;
				if(numFound > 0) {
					if(settings.anyTerm) {
						found = true;
					}
				} else {
					if(!settings.anyTerm) {
						found = false;
					}
				}
			}
			$(this).toggleClass("found", found);
			$(this).toggleClass("not-found", !found);
			return $(this);
		});
		var parentFound = changed.filter(".search-node.found .search-node.not-found");
		var childFound = changed.filter(".search-node.not-found:has(.search-node.found)");
		parentFound.removeClass("not-found");
		parentFound.addClass("parent-found");
		childFound.removeClass("not-found");
		childFound.addClass("child-found");
		if(settings.resetOnBlank && !text) {
			settings.onReset();
		}
		settings.afterSearch();
		return changed;
	};
	
	$.expr[":"].Contains = $.expr.createPseudo(function(arg) {
	    return function( elem ) {
	        return $(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
	    };
	});
 
}( jQuery ));