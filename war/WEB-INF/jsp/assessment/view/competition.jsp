<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1 style="margin-bottom:0.5em;">${competition.name}</h1>

<ul class="list">
<jsp:include page="../../recursive/fileWriter.jsp"/>
</ul>

<button id="newPopup">Update competition Code</button>

<form:form commandName="competition" enctype="multipart/form-data" method="POST">
	<table>
		<tr><td>Has been tested:</td><td class="pastaTF pastaTF${competition.tested}">${competition.tested}</td></tr>
		<tr><td>Is live:</td><td class="pastaTF pastaTF${competition.live}">${competition.live}</td></tr>
		<c:choose>
			<c:when test="${competition.calculated}">
				<tr><td>Competition Type:</td><td>Calculated</td></tr>
			</c:when>
			<c:otherwise>
				<tr><td>Competition Type:</td><td>Arena</td></tr>
				<!-- can tutors make repeatable arenas -->
				<tr><td>Tutors can create repeatable arenas:</td><td><form:checkbox path="tutorCreatableRepeatableArena"/></td></tr>
				<!-- can students make arenas -->
				<tr><td>Students can create arenas:</td><td><form:checkbox path="studentCreatableArena"/></td></tr>
				<!-- can students make repeatable arenas -->
				<tr><td>Students can create repeatable arenas:</td><td><form:checkbox path="studentCreatableRepeatableArena"/></td></tr>
			</c:otherwise>
		</c:choose>
		<tr><td>First run:</td><td><form:input path="firstStartDateStr"/></td></tr>
		<tr>
			<td>Frequency:</td>
			<td>
				<form:input type="number" path="frequency.years" style="width:3em;"/> years
				<form:input type="number" path="frequency.days" style="width:3em;"/> days
				<form:input type="number" path="frequency.hours" style="width:3em;"/> hours
				<form:input type="number" path="frequency.minutes" style="width:3em;"/> minutes
				<form:input type="number" path="frequency.seconds" style="width:3em;"/> seconds
			</td>
		</tr>
		<c:choose>
			<c:when test="${empty competition.nextRunDate }">
				Never
			</c:when>
			<c:otherwise>
				<tr><td>Next Run:</td><td>${competition.nextRunDate}</td></tr>
			</c:otherwise>
		</c:choose>
	</table> 
	
	<input type="submit" value="Save Competition" id="submit" style="margin-top:1em;"/>
</form:form>

<c:if test="${not competition.calculated}">
	<table style="width	:100%;">
		<tr><th>Name</th><th>First Run Date</th><th>Repeating Frequency</th><th>Password protected</th></tr>
		<c:forEach var="arena" items="${competition.arenas}">
			<tr>
				<td>${arena.name}</td>
				<td>${arena.firstStartDate}</td>
				<td>
					<c:choose>
						<c:when test="${arena.repeatable}">
							${arena.frequency.niceStringRepresentation}
						</c:when>
						<c:otherwise>
							Does not repeat
						</c:otherwise>
					</c:choose>
				</td>
				<td>
				<c:choose>
					<c:when test="${arena.passwordProtected}">
						YES 
					</c:when>
					<c:otherwise>
						NO
					</c:otherwise>
				</c:choose>
				</td>
			</tr>
		</c:forEach>
	</table>
</c:if>

<div id="newCompetition" >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> Update competition code </h1>
	<form:form commandName="newCompetitionModel" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Competition Code:</td><td><form:input type="file" path="file"/></td></tr>
		</table>
    	<input type="submit" value="Create" id="submit"/>
	</form:form>
</div>

	
<script>
	;(function($) {

         // DOM Ready
        $(function() {
        	
        	$('#firstStartDateStr').datetimepicker({ dateFormat: 'dd/mm/yy', timeFormat: 'hh:mm' });
        
            // Binding a click event
            // From jQuery v.1.7.0 use .on() instead of .bind()
            $('#newPopup').bind('click', function(e) {

                // Prevents the default action to be triggered. 
                e.preventDefault();

                // Triggering bPopup when click event is fired
                $('#newCompetition').bPopup();

            });
            
        });

    })(jQuery);
</script>