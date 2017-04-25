(function(window, $) {
	/**
	  * <p> Gets a colour that is {percentage} way through the colour gradient defined
	  * by {colours} and {stops}
	  * 
	  * <p> e.g. <br/>
	  * colour(0.5, ["red", "green"]) - results in a colour 50% of the way between
	  * red and green
	  * 
	  * <p> colour([0, 0.25, 0.75], ["red", "rgba(255,0,0,0)"], [0, 0.5]) results in an array
	  * of three 'colours'. The gradient starts at red at 0%, then goes to transparent
	  * red at 50%. The gradient stays transparent red until 100%.
	  * 
	  * @param percentages - either a single percentage [0,1] or an array of percentages
	  * 
	  * @param colours - either a single colour or an array or colours. Each colour is a 
	  * string, and accepts any colour format accespted by CSS (e.g. "red", 
	  * "rgb(255,0,0)", "#FF0000").
	  * 
	  * @param stops (optional) - either a single percentage value or an array of 
	  * percentages. Each stop defines the point at which the corresponding colour is 
	  * full. If left out, the default will be even spacing of colours along the gradient.
	  * 
	  * @returns either a single 4D vector if only one percentage was provided, or an array 
	  * of 4D vectors if an array of percentages is provided.
	  */
	function gradientColour(percentages, colours, stops) {
		if(percentages !== undefined && !Array.isArray(percentages)) {
			percentages = [percentages];
		}
		if(colours !== undefined && !Array.isArray(colours)) {
			colours = [colours];
		}
		if(stops !== undefined && !Array.isArray(stops)) {
			stops = [stops];
		}
		if(!colours.length || (stops && colours.length != stops.length)) {
			return undefined;
		}
		if(!stops) {
			stops = [];
			if(colours.length > 1) {
				for(var i = 0; i < colours.length; i++) {
					stops.push(i / (colours.length - 1));
				}
			} else {
				stops.push(1);
			}
		}
		for(var i = 0; i < colours.length; i++) {
			colours[i] = parseColour(colours[i]);
		}
		if(stops[0] > 0) {
			stops.unshift(0);
			colours.unshift(colours[0].slice());
		}
		if(stops[stops.length-1] < 1) {
			stops.push(1);
			colours.push(colours[colours.length-1].slice());
		}

		var newColours = [];
		for(var i = 0; i < percentages.length; i++) {
			var p = percentages[i];
			if(p < 0 || p > 1) {
				newColours.push(undefined);
			}
			var c1, c2, v1, v2;
			for(var j = 1; j < colours.length; j++) {
				if(stops[j] >= p) {
					c1 = colours[j-1];
					c2 = colours[j];
					v1 = stops[j-1];
					v2 = stops[j];
					break;
				}
			}
			if(!c1) {
				c1 = colours[0]; c2= colours[0];
				v1 = stops[0]; v2= stops[0];
			}

			var newColour = [];
			for(var n = 0; n < c1.length; n++) {
				var perc = v2 == v1 ? 1 : (p-v1)/(v2-v1);
				var val = valueInRange(c1[n], c2[n], perc);
				if(n <= 2) {
					val = Math.round(val);
				}
				newColour.push(val);
			}
			newColours.push(toColourString(newColour));
		}
		if(percentages.length == 1) {
			return newColours[0];
		} else {
			return newColours;
		}
	}

	function valueInRange(min, max, percentage) {
		return Math.max(0, Math.min(min + ((max - min) * percentage), 255));
	}

	/**
	 * Convert a colour string into values suitable for the format rgba(r,g,b,a)
	 * @param colour the String colour, in any format accepted by CSS
	 * @returns a 4D array of values: [[0-255],[0-255],[0-255],[0-1]]
	 */
	function parseColour(colour) {
		var div = $("<div/>").css('color', colour);
		$('body').append(div);
		colour = String(div.css('color'));
		div.remove();
		try {
			var parts = colour.replace(/\s+/g, "").split('(')[1].split(')')[0].split(',');
			for(var i = 0; i < parts.length; i++) parts[i] = +parts[i];
			while(parts.length < 4) {
				parts.push(1);
			}
			return parts;
		} catch(e) {
			return [0,0,0,0];
		}
	}

	function brighter(colour, amount = 0.2) {
		var hsv = rgbToHsv(colour);
		hsv[1] = Math.max(0, Math.min(hsv[1] - amount, 1));
		hsv[2] = Math.max(0, Math.min(hsv[2] + amount, 1));
		return toColourString(hsvToRgb(hsv));
	}
	function darker(colour, amount = 0.2) {
		return brighter(colour, -amount);
	}
	function percentBrightness(colour, amount = 1.0) {
		var hsv = rgbToHsv(colour);
		hsv[1] = Math.max(0, Math.min(hsv[1] / amount, 1));
		hsv[2] = Math.max(0, Math.min(hsv[2] * amount, 1));
		return hsvToRgb(hsv);
	}

	function toColourString(colour) {
		var method = colour.length == 3 ? 'rgb' : 'rgba';
		var str = method + '(';
		for(var i = 0; i < colour.length; i++) {
			if(i > 0) str += ',';
			str += colour[i];
		}
		return str + ')';
	}

	function useWhiteText(rgbColor) {
		if(!Array.isArray(rgbColor)) {
			rgbColor = parseColour(rgbColor);
		}
		var r = rgbColor[0], g = rgbColor[1], b = rgbColor[2];
		var yiq = ((r*299)+(g*587)+(b*114))/1000;
		return yiq < 128;
	}

	/**
	 * Adapted from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c
	 */
	function rgbToHsv(colour){
		if(!Array.isArray(colour)) {
			colour = parseColour(colour);
		}
	    var r = colour[0] / 255, g = colour[1] / 255, b = colour[2] / 255, a = colour[3];
	    var max = Math.max(r, g, b), min = Math.min(r, g, b);
	    var h, s, v = max;

	    var d = max - min;
	    s = max == 0 ? 0 : d / max;

	    if(max == min){
	        h = 0; // achromatic
	    }else{
	        switch(max){
	            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
	            case g: h = (b - r) / d + 2; break;
	            case b: h = (r - g) / d + 4; break;
	        }
	        h /= 6;
	    }

	    return [h, s, v, a];
	}

	/**
	 * Adapted from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c
	 */
	function hsvToRgb(colour){
		var h = colour[0], s = colour[1], v = colour[2], a = colour[3];
		var r, g, b;

	    var i = Math.floor(h * 6);
	    var f = h * 6 - i;
	    var p = v * (1 - s);
	    var q = v * (1 - f * s);
	    var t = v * (1 - (1 - f) * s);

	    switch(i % 6){
	        case 0: r = v, g = t, b = p; break;
	        case 1: r = q, g = v, b = p; break;
	        case 2: r = p, g = v, b = t; break;
	        case 3: r = p, g = q, b = v; break;
	        case 4: r = t, g = p, b = v; break;
	        case 5: r = v, g = p, b = q; break;
	    }

	    return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255), a];
	}

	/**
	 * Adapted from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c
	 */
	function rgbToHsl(colour){
		if(!Array.isArray(colour)) {
			colour = parseColour(colour);
		}
	    var r = colour[0] / 255, g = colour[1] / 255, b = colour[2] / 255, a = colour[3];
	    var max = Math.max(r, g, b), min = Math.min(r, g, b);
	    var h, s, l = (max + min) / 2;

	    if(max == min){
	        h = s = 0; // achromatic
	    }else{
	        var d = max - min;
	        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
	        switch(max){
	            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
	            case g: h = (b - r) / d + 2; break;
	            case b: h = (r - g) / d + 4; break;
	        }
	        h /= 6;
	    }

	    return [h, s, l, a];
	}

	/**
	 * Adapted from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c
	 */
	function hslToRgb(colour){
	    var r, g, b;
	    var h = colour[0], s = colour[1], l = colour[2], a = colour[3];

	    if(s == 0){
	        r = g = b = l; // achromatic
	    }else{
	        function hue2rgb(p, q, t){
	            if(t < 0) t += 1;
	            if(t > 1) t -= 1;
	            if(t < 1/6) return p + (q - p) * 6 * t;
	            if(t < 1/2) return q;
	            if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
	            return p;
	        }

	        var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
	        var p = 2 * l - q;
	        r = hue2rgb(p, q, h + 1/3);
	        g = hue2rgb(p, q, h);
	        b = hue2rgb(p, q, h - 1/3);
	    }

	    return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255), a];
	}
	
	var Colours = {};
	Colours.gradientColour = gradientColour;
	Colours.brighter = brighter;
	Colours.darker = darker;
	Colours.percentBrightness = percentBrightness;
	Colours.useWhiteText = useWhiteText;
	Colours.rgbToHsv = rgbToHsv;
	Colours.hsvToRgb = hsvToRgb;
	Colours.rgbToHsl = rgbToHsl;
	Colours.hslToRgb = hslToRgb;
	window['Colours'] = Colours; 
})(window, jQuery);




