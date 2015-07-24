<!-- 
Copyright (c) 2015, Joshua Stretton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
-->

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<h1>Convert Legacy PASTA Content</h1>

<div class='vertical-block'>
	<p>Use this page to convert old PASTA content into current PASTA content. 
	
	<p>Place the old <code>content</code> folder inside the current <code>content</code> folder, and name the old content folder "<code>legacy</code>", then press "Convert".
	
	<p>This requires instructor level access.
	
	<p>It is suggested that you delete the legacy content from the legacy folder after running this, as running it again will create duplicate components.
</div>
<div id='button-div' class='vertical-block'>
	<button id='convert-button'>Convert</button>
</div>
<div id='output-div' class='vertical-block'>
	<textarea id='output' rows="30" style='width:90%' readonly="readonly"></textarea>
</div>

<script>
	var statusTimer;

    $(function() {
    	$("#output-div").hide();
    	<c:if test="${started}">
	    	$("#button-div").hide();
	    	$("#output-div").show();
	    	checkStatus();
    	</c:if>
    	$('#convert-button').on("click", function() {
    		startConvert();
    	});
    });
    
    function startConvert() {
    	$("#button-div").hide();
    	$("#output-div").show();
    	$.ajax({
			type : "POST",
			statusCode : {
				500 : function(jqXHR, textStatus, errorThrown) {
					alert("Failed to start converting.");
				}
			},
			success : function(data) {
				if(data) {
					alert("Failed to start converting.");
				} else {
					checkStatus();
				}
			}
		});
    }
    
    function checkStatus() {
    	$.ajax({
			url : "status/",
			type : "POST",
			statusCode : {
				500 : function(jqXHR, textStatus, errorThrown) {
					alert("Failed to read output.");
				}
			},
			success : function(data) {
				if(data == "NOT AUTHORISED" || data == "NOT STARTED" || data == "DONE") {
					finishConvert();
				} else {
					updateOutput(data);
					statusTimer = setTimeout(checkStatus, 1000);
				}
			}
		});
    }
    
    function finishConvert() {
    	updateOutput("Finished.");
    	clearTimeout(statusTimer);
    }
    
    function updateOutput(data) {
    	$('#output').append(data);
    }
</script>
