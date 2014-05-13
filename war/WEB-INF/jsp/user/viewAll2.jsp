<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Ignore me, I don't exist -->
<div style="display:none" class="gradeCentreMarkGood"></div>
<div style="display:none" class="gradeCentreMarkBad"></div>
<div style="display:none" class="gradeCentreMarkNoSub"></div>

<div style="float: left; width:100%">
	<button style="float: left; text-align: center;"
		onclick="window.location = '../downloadMarks/'">Download Marks</button>
</div>
<div style="float: left; width:100%">
	<button style="float: left; text-align: center;"
		onclick="window.location = '../downloadAutoMarks/'">Download Auto Marks ONLY</button>
</div>

<style>
	th, td { white-space: nowrap; }
	div.dataTables_wrapper {
		width: 800px;
		margin: 0 auto;
	}
</style>

<table id="gradeCentreTable"  class="display" cellspacing="0" width="100%">
	<thead>
		<tr>
			<th>Username</th>
			<th>Stream</th>
			<th>Class</th>
			<c:forEach var="assessment" items="${assessmentList}">
				<th>${assessment.name}</th>
			</c:forEach>
		</tr>
	</thead>
</table>

<script>
	$(document).ready(function() 
	    { 			
			var oTable = $('#gradeCentreTable').dataTable({
				"scrollX": true,
				"iDisplayLength": 25,
				"ajax": "DATA/"
			} );
	    } 
	); 

	var clr = $("div.gradeCentreMarkGood").css("backgroundColor").replace("rgb(","").replace(")","");
	var yr = parseFloat(clr.split(",")[0]);
	var yg = parseFloat(clr.split(",")[1]);
	var yb = parseFloat(clr.split(",")[2]);
		
	var clrBad = $("div.gradeCentreMarkBad").css("backgroundColor").replace("rgb(","").replace(")","");
	var xr = parseFloat(clrBad.split(",")[0]);
	var xg = parseFloat(clrBad.split(",")[1]);
	var xb = parseFloat(clrBad.split(",")[2]);
	
	$("td.gradeCentreMark").each(function(){
	
		// change them according to the percentage
		if(this.getElementsByTagName("span")[0].innerHTML == ""){
			$(this).css({backgroundColor:$("div.gradeCentreMarkNoSub").css("backgroundColor")});
		}
		else{
			var pos = parseFloat(this.getElementsByTagName("span")[0].innerHTML);
			
			n = 100; // number of color groups
			
			red = parseInt((xr + (( pos * (yr - xr)))).toFixed(0));
			green = parseInt((xg + (( pos * (yg - xg)))).toFixed(0));
			blue = parseInt((xb + (( pos * (yb - xb)))).toFixed(0));

			$(this).css({backgroundColor:'rgb('+red+','+green+','+blue+')'});
		}
			
	});
</script>
