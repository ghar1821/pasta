<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h1>Convert Legacy PASTA Content</h1>

<div class='section'>
	<div class='part'>
		<p>Use this page to convert old PASTA content into current PASTA content. 
		
		<p>Place the old <code>content</code> folder inside the current <code>content</code> folder, and name the old content folder "<code>legacy</code>", then press "Convert".
		
		<p>This requires instructor level access.
		
		<p>It is suggested that you delete the legacy content from the legacy folder after running this, as running it again will create duplicate components.
		
		<div class='button-pannel'>
			<button class='button' id='convert-button'>Convert</button>
		</div>
	</div>
</div>
<div id='output-div' class='section'>
	<div class='part'>
		<textarea id='output' rows="30" style='width:90%' readonly="readonly"></textarea>
	</div>
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
