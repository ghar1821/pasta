<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Assessments</h1>

<table class="pastaTable">
	<tr>
		<th>Status</th>
		<th>Name</th>
		<th>Due Date</th>
		<th>Marks</th>
		<th># Submissions Allowed</th>
		<th>Tests</th>
		<!-- <th>Delete</th>   deprecated delete?-->
		<th>Release</th>
	</tr>
	<c:forEach var="assessment" items="${allAssessments}">
		<tr>
			<td>
				<c:if test="${not assessment.completelyTested}">
					<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;" title="Contains untested unit tests."></span>
				</c:if>
				<c:if test="${assessment.closed}">
					<span class="ui-icon ui-icon-locked" style="float: left; margin-right: .3em;" title="Past due date"></span>
				</c:if>
				<c:if test="${not assessment.released}">
					<span class="ui-icon ui-icon-gear" style="float: left; margin-right: .3em;" title="Not released"></span>
				</c:if>
			</td>
			<td><a href="./${assessment.shortName}/">${assessment.name}</a>
			<smallbutton id="delete" style="margin-left:-.1em; position:relative; top:-.7em" onClick="document.getElementById('comfirmButton').onclick = function(){ location.href='./delete/${assessment.shortName}/'};$('#comfirmPopup').bPopup();">X</smallbutton></td>
			<td>${assessment.dueDate}</td><td>${assessment.marks}</td>
			<td>${assessment.numSubmissionsAllowed > 0 ? assessment.numSubmissionsAllowed : '&infin;'}</td>
			<td>${fn:length(assessment.unitTests)}u, ${fn:length(assessment.secretUnitTests)}su, TODO hm, TODOc</td>
			<!--<td><smallbutton id="delete" onClick="document.getElementById('comfirmButton').onclick = function(){ location.href='./delete/${assessment.shortName}/'};$('#comfirmPopup').bPopup();">X</smallbutton></td>-->
			<td><button id="delete" onClick="document.getElementById('comfirmButton').onclick = function(){ location.href='./release/${assessment.shortName}/'};$('#comfirmPopup').bPopup();"><span class="ui-icon ui-icon-gear" style="float: left; margin-right: .3em;" title="Not released"></span></button></td>
		</tr>
	</c:forEach>
</table> 

<button id="newPopup">Add a new Assessment</button>

<div id="newAssessment" >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> New Assessment </h1>
	<form:form commandName="assessment" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Assessment Name:</td><td><form:input autocomplete="off" type="text" path="name" value=""/></td></tr>
			<tr><td>Assessment Marks:</td><td><form:input type="text" path="marks"/></td></tr>
			<tr><td>Assessment DueDate:</td><td><form:input type="text" path="simpleDueDate" id="simpleDueDate" name="simpleDueDate"/></td></tr>
			<tr><td>Maximum Number of allowed submissions:</td><td><form:input type="text" path="numSubmissionsAllowed"/></td></tr>
		</table>
    	<input type="submit" value="Create" id="submit"/>
	</form:form>
</div>

<div id="comfirmPopup" >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1>Are you sure you want to do that?</h1>
	<button id="comfirmButton" onClick="">Confirm</button>
</div>
	
<script>
	;(function($) {

         // DOM Ready
        $(function() {
        
        	$( "#simpleDueDate" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});// TODO

            // Binding a click event
            // From jQuery v.1.7.0 use .on() instead of .bind()
            $('#newPopup').bind('click', function(e) {

                // Prevents the default action to be triggered. 
                e.preventDefault();

                // Triggering bPopup when click event is fired
                $('#newAssessment').bPopup();

            });
        });
        
    })(jQuery);
</script>
