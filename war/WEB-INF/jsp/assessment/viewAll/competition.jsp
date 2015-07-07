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
		<c:if test="${not empty liveAssessmentCounts[competition.id] and liveAssessmentCounts[competition.id] gt 0}">
			<c:set var="liveAssessment" value="true"/>
		</c:if>
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
					Used in ${liveAssessmentCounts[competition.id]} Assessment<c:if test="${liveAssessmentCounts[competition.id] != 1}">s</c:if>
				</td>
				<td>
					<!-- buttons -->
					<c:if test="${unikey.tutor}">
						<div style="float:left">
							<button style="float:left; text-align: center; " onclick="location.href='./${competition.id}/'">Details</button>
						</div>
					</c:if>
					<c:if test="${not competition.calculated}">
						<div style="float:left">
							<button style="float:left; text-align: center; " onclick="location.href='./${competition.id}/myPlayers/'">My Players</button>
						</div>
					</c:if>
					<div style="float:left">
						<button style="float:left; text-align: center; " onclick="location.href='./view/${competition.id}/'">Competition Page</button>
					</div>
					<c:if test="${unikey.tutor}">
						<div style="float:left">
							<button style="float:left; text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
							<button style="float:left; display:none; text-align: center; " onclick="location.href='./delete/${competition.id}/'" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
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
				<tr><td>Competition Name:</td>
					<td>
						<form:input path="name"/> 
						<form:errors path="name" />
					</td>
				</tr>
				<tr><td>Competition Type:</td>
					<td>
						<form:select path="type">
							<option value="calculated">Calculated</option>
							<option value="arena">Arena</option>
						</form:select> 
						<form:errors path="type" />
					</td>
				</tr>
			</table>
	    	<input type="submit" value="Create" id="submit"/>
		</form:form>
	</div>
	
		
	<script>
        $(function() {
            $('#newPopup').on('click', function(e) {
                e.preventDefault();
                $('.popup').bPopup();
            });
            
	        <spring:hasBindErrors name='newCompetitionModel'>
				$('.popup').bPopup();
	    	</spring:hasBindErrors>
        });
	</script>
</c:if>
