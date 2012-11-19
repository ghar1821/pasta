<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Assessments</h1>

<table class="pastaTable">
	<tr><th>Name</th><th>Due Date</th><th>Marks</th><th># Submissions Allowed</th><th># Public Unit Tests</th><th># Secret Unit Tests</th><th># Hand Marking</th><th># Competitions</th></tr>
	<c:forEach var="assessment" items="${allAssessments}">
		<tr>
			<td><a href="./${assessment.shortName}/">${assessment.name}</a></td>
			<td>${assessment.dueDate}</td><td>${assessment.marks}</td>
			<td>${assessment.numSubmissionsAllowed > 0 ? assessment.numSubmissionsAllowed : '&infin;'}</td>
			<td>${fn:length(assessment.unitTests)}</td>
			<td>${fn:length(assessment.secretUnitTests)}</td>
			<td>TODO</td>
			<td>TODO</td>
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
