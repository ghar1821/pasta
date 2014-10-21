<!-- 
Copyright (c) 2014, Alex Radu
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
		<tr><td>Hidden competition:</td><td><form:checkbox path="hidden"/></td></tr>
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
		<tr>
			<td>${competition.officialArena.name}</td>
			<td>${competition.officialArena.firstStartDate}</td>
			<td>
				<c:choose>
					<c:when test="${competition.officialArena.repeatable}">
						${competition.officialArena.frequency.niceStringRepresentation}
					</c:when>
					<c:otherwise>
						Does not repeat
					</c:otherwise>
				</c:choose>
			</td>
			<td>
			<c:choose>
				<c:when test="${competition.officialArena.passwordProtected}">
					YES 
				</c:when>
				<c:otherwise>
					NO
				</c:otherwise>
			</c:choose>
			</td>
		</tr>
		<c:forEach var="arena" items="${competition.outstandingArenas}">
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
		<c:forEach var="arena" items="${competition.completedArenas}">
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
    	<input type="submit" value="Update" id="submit"/>
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