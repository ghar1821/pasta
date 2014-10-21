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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:choose>
	<c:when test="${ not empty viewedUser}">
		<h1>${viewedUser.username}</h1>
		<c:set var="classes" value="${viewedUser.stream}.${viewedUser.tutorial}"/>
		<c:set var="releaseUsername" value="${viewedUser.username}"/>
	</c:when>
	<c:otherwise>
		<h1>${unikey.username}</h1>
		<c:set var="classes" value="${unikey.stream}.${unikey.tutorial}"/>
		<c:set var="releaseUsername" value="${unikey.username}"/>
	</c:otherwise>
</c:choose>

<h1>Competitions</h1>

<table class="pastaTable">
	<c:forEach var="competition" items="${allCompetitions}">
		<c:set var="liveAssessment" value="false"/>
		<c:forEach var="assessment" items="${competition.linkedAssessments}" >
				<c:if test="${unikey.tutor or ((not empty assessment.releasedClasses) and ( fn:contains(assessment.releasedClasses, classes))) or ((not empty assessment.specialRelease) and ( fn:contains(assessment.specialRelease, releaseUsername)))}">
					<c:set var="liveAssessment" value="true"/>
				</c:if>
		</c:forEach>
		<c:if test="${(liveAssessment and not competition.hidden) or unikey.tutor}" >
			<tr>		
			<!-- 
				<td class="pastaTF pastaTF${competition.tested}">
					<!-- status ->
					<c:choose>
						<c:when test="${competition.tested}">
							TESTED
						</c:when>
						<c:otherwise>
							UNTESTED
						</c:otherwise>
					</c:choose>
				</td>
				 -->
				<td>
					<!-- name -->
					<b>${competition.name}</b><br/>
					<c:choose>
						<c:when test="${competition.calculated}">
							Calculated Competition
						</c:when>
						<c:otherwise>
							Arena Competition <br/>
							${fn:length(competition.outstandingArenas) + 1} live arenas registered
						</c:otherwise>
					</c:choose>
					<br/>
					<c:choose>
						<c:when test="${empty competition.nextRunDate }">
							Will never run
						</c:when>
						<c:otherwise>
							Next Run: ${competition.nextRunDate}
						</c:otherwise>
					</c:choose>
					<br/>
					Used in ${fn:length(competition.linkedAssessments)} Assessment<c:if test="${fn:length(competition.linkedAssessments) != 1}">s</c:if>
				</td>
				<td>
					<!-- buttons -->
					<c:if test="${unikey.tutor}">
						<div style="float:left">
							<button style="float:left; text-align: center; " onclick="location.href='./${competition.shortName}/'">Details</button>
						</div>
					</c:if>
					<c:if test="${not competition.calculated}">
						<div style="float:left">
							<button style="float:left; text-align: center; " onclick="location.href='./${competition.shortName}/myPlayers/'">My Players</button>
						</div>
					</c:if>
					<div style="float:left">
						<button style="float:left; text-align: center; " onclick="location.href='./view/${competition.shortName}/'">Competition Page</button>
					</div>
					<c:if test="${unikey.tutor}">
						<div style="float:left">
							<button style="float:left; text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
							<button style="float:left; display:none; text-align: center; " onclick="location.href='./delete/${competition.shortName}/'" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
						</div>
					</c:if>
				</td>
			</tr>
		</c:if>
	</c:forEach>
</table>
				
<c:if test="${unikey.tutor}">

	<button id="newPopup">Add a new Competition</button>
	
	<div id="newCompetition" class="popup">
		<span class="button bClose">
			<span><b>X</b></span>
		</span>
		<h1> New Competition </h1>
		<form:form commandName="newCompetitionModel" enctype="multipart/form-data" method="POST">
			<table>
				<tr><td>Competition Name:</td><td><form:input autocomplete="off" type="text" path="name" value=""/></td></tr>
				<tr><td>Competition Type:</td>
					<td>
						<form:select path="type">
							<option value="calculated">Calculated</option>
							<option value="arena">Arena</option>
						</form:select>
					</td>
				</tr>
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
				<tr><td>Hidden competition:</td><td><form:checkbox path="hidden"/></td></tr>
				<tr><td>Competition Code:</td><td><form:input type="file" path="file"/></td></tr>
			</table>
	    	<input type="submit" value="Create" id="submit"/>
		</form:form>
	</div>
	
		
	<script>
		;(function($) {
	
	         // DOM Ready
	        $(function() {
	        	
	        	$("#firstStartDateStr").datetimepicker({
					timeformat : 'hh:mm',
					dateFormat : 'dd/mm/yy'
				});// TODO
	        
	            // Binding a click event
	            // From jQuery v.1.7.0 use .on() instead of .bind()
	            $('#newPopup').bind('click', function(e) {
	
	                // Prevents the default action to be triggered. 
	                e.preventDefault();
	
	                // Triggering bPopup when click event is fired
	                $('.popup').bPopup();
	
	            });
	            
	        });
	
	    })(jQuery);
	</script>
</c:if>
