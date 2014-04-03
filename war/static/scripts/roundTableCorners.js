/*
 Copyright (c) 2010 Don Magee
 
 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.
 */
 
(function($){
    $.fn.tableCorners = function(options) {
 
        var defaults = {
            collapse: true,
            thead: true,
            tbody: true,
            tfoot: false,
            radius: '4px'
        };
        var options = $.extend(defaults, options);
 
        return this.each(function() {
            obj = $(this);
            $(this).css('-moz-border-radius', options.radius);
            $(this).css('-webkit-border-radius', options.radius);
            if(options.collapse)
            {
                obj.attr('cellspacing', '0');
            }
            if(options.thead)
            {
                $(obj).find('thead tr:first th:first').css('-moz-border-radius-topleft', options.radius);
                $(obj).find('thead tr:first th:first').css('-webkit-border-top-left-radius', options.radius);
                $(obj).find('thead tr:first th:last').css('-moz-border-radius-topright', options.radius);
                $(obj).find('thead tr:first th:last').css('-webkit-border-top-right-radius', options.radius);
            }
            if(options.tbody)
            {
                if(!options.thead)
                {
                    $(obj).find('tbody tr:first td:first').css('-moz-border-radius-topleft', options.radius);
                    $(obj).find('tbody tr:first td:first').css('-webkit-border-top-left-radius', options.radius);
                    $(obj).find('tbody tr:first td:last').css('-moz-border-radius-topright', options.radius);
                    $(obj).find('tbody tr:first td:last').css('-webkit-border-top-right-radius', options.radius);
                }
                if(!options.tfooter)
                {
                    $(obj).find('tbody tr:last td:first').css('-moz-border-radius-bottomleft', options.radius);
                    $(obj).find('tbody tr:last td:first').css('-webkit-border-bottom-left-radius', options.radius);
                    $(obj).find('tbody tr:last td:last').css('-moz-border-radius-bottomright', options.radius);
                    $(obj).find('tbody tr:last td:last').css('-webkit-border-bottom-right-radius', options.radius);
                }
            }
            if(options.tfoot)
            {
                $(obj).find('tfoot tr:last td:first').css('-moz-border-radius-bottomleft', options.radius);
                $(obj).find('tfoot tr:last td:first').css('-webkit-border-bottom-left-radius', options.radius);
                $(obj).find('tfoot tr:last td:last').css('-moz-border-radius-bottomright', options.radius);
                $(obj).find('tfoot tr:last td:last').css('-webkit-border-bottom-right-radius', options.radius);
            }
            if(!options.tbody && !options.thead && !options.tfoot)
            {
                $(obj).find('tr:first td:first').css('-moz-border-radius-topleft', options.radius);
                $(obj).find('tr:first td:first').css('-webkit-border-top-left-radius', options.radius);
                $(obj).find('tr:first td:last').css('-moz-border-radius-topright', options.radius);
                $(obj).find('tr:first td:last').css('-webkit-border-top-right-radius', options.radius);
                $(obj).find('tr:last td:first').css('-moz-border-radius-bottomleft', options.radius);
                $(obj).find('tr:last td:first').css('-webkit-border-bottom-left-radius', options.radius);
                $(obj).find('tr:last td:last').css('-moz-border-radius-bottomright', options.radius);
                $(obj).find('tr:last td:last').css('-webkit-border-bottom-right-radius', options.radius);
            }
        });
    };
})(jQuery);