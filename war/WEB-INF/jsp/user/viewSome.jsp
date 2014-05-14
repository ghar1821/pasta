<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Ignore me, I don't exist -->
<div style="display:none" class="gradeCentreMarkGood"></div>
<div style="display:none" class="gradeCentreMarkBad"></div>
<div style="display:none" class="gradeCentreMarkNoSub"></div>

<script>
	var clr = $("div.gradeCentreMarkGood").css("backgroundColor").replace("rgb(","").replace(")","");
	var yr = parseFloat(clr.split(",")[0]);
	var yg = parseFloat(clr.split(",")[1]);
	var yb = parseFloat(clr.split(",")[2]);
		
	var clrBad = $("div.gradeCentreMarkBad").css("backgroundColor").replace("rgb(","").replace(")","");
	var xr = parseFloat(clrBad.split(",")[0]);
	var xg = parseFloat(clrBad.split(",")[1]);
	var xb = parseFloat(clrBad.split(",")[2]);
</script>

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
			<th>Username&nbsp;&nbsp;&nbsp;&nbsp;</th>
			<th>Stream&nbsp;&nbsp;&nbsp;&nbsp;</th>
			<th>Class&nbsp;&nbsp;&nbsp;&nbsp;</th>
			<c:forEach var="assessment" items="${assessmentList}">
				<th>${assessment.name}&nbsp;&nbsp;&nbsp;&nbsp;</th>
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
				"ajax": "DATA/",
				"deferRender": true,
		        "columns": [
					{ "mData": "name" },
					{ "mData": "stream" },
					{ "mData": "class" },
					<c:forEach var="assessment" items="${assessmentList}" varStatus="assessmentStatus">
					{ "mData": {_: "${assessment.shortName}", sort: "${assessment.shortName}.percentage"}}<c:if test="${assessmentStatus.index < (fn:length(assessmentList)-1)}">,</c:if>
					</c:forEach>
		         ],
				 "aoColumnDefs": [ {
					  "aTargets": ["_all"],
					  "mRender": function ( data, type, full ) {
						// assessment
						if (data.mark >= 0) {
							return '<span style="display:none">'+data.percentage+'</span><a href="../../student/'+full.name+'/info/'+data.assessmentname+'/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">'+data.mark+'</a>';
						}
						// name
						if(data == full.name){
							return '<a href="../../student/'+data+'/home/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">'+data+'</a>';
						}
						// stream
						if(data == full.stream){
							return '<a href="../../stream/'+data+'/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">'+data+'</a>';
						}
						// class
						if(data == full.class){
							return '<a href="../../tutorial/'+data+'/" style="display:block;height:100%;width:100%;text-decoration:none;color:black;">'+data+'</a>';
						}
						return data;
					  },
					  "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
						if(iCol > 2){
							if ( nTd.getElementsByTagName("span")[0].innerHTML == "" ) {
							  $(nTd).css('background-color', $("div.gradeCentreMarkNoSub").css("backgroundColor"));
							}
							else{
								var pos = parseFloat(nTd.getElementsByTagName("span")[0].innerHTML);
								
								n = 100; // number of color groups
								
								red = parseInt((xr + (( pos * (yr - xr)))).toFixed(0));
								green = parseInt((xg + (( pos * (yg - xg)))).toFixed(0));
								blue = parseInt((xb + (( pos * (yb - xb)))).toFixed(0));

								$(nTd).css('background-color', 'rgb('+red+','+green+','+blue+')');
							}
						}
					  }
					} ]
			} );
	    } 
	); 

</script>

