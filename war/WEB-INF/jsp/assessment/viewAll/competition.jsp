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
		<c:set var="classes" value="${viewedUser.stream}.${viewedUser.tutorial}"/>
		<c:set var="releaseUsername" value="${viewedUser.username}"/>
	</c:when>
	<c:otherwise>
		<c:set var="classes" value="${user.stream}.${user.tutorial}"/>
		<c:set var="releaseUsername" value="${user.username}"/>
	</c:otherwise>
</c:choose>

<h1>Competitions - ${releaseUsername}</h1>

<div class='section'>
	<c:forEach var="competition" items="${allCompetitions}">
		<c:set var="liveAssessment" value="false"/>
		<c:if test="${not empty liveAssessmentCounts[competition.id] and liveAssessmentCounts[competition.id] gt 0}">
			<c:set var="liveAssessment" value="true"/>
		</c:if>
		<c:if test="${(liveAssessment and not competition.hidden) or user.tutor}" >
			<div class='part'>
				<div class='part-title larger-text'>${competition.name}</div>
				<div class='part-title subtitle'>${competition.calculated ? "Calculated Competition" : "Arena Competition"}</div>
				
				<div class='info-panel horizontal-block top-align'>
					<div class='ip-item'>
						<div class='ip-label'>Used in:</div>
						<div class='ip-desc'>${liveAssessmentCounts[competition.id]} Assessment<c:if test="${liveAssessmentCounts[competition.id] != 1}">s</c:if></div>
					</div>
				</div>
				<div class='info-panel horizontal-block top-align'>
					<div class='ip-item'>
						<div class='ip-label'>Next Run:</div>
						<div class='ip-desc'>
							<c:choose>
								<c:when test="${empty competition.nextRunDate }">
									Never
								</c:when>
								<c:otherwise>
									${competition.nextRunDate}
								</c:otherwise>
							</c:choose>
						</div>
					</div>
				</div>
				<c:if test="${not competition.calculated}">
					<div class='info-panel horizontal-block top-align'>
						<div class='ip-item'>
							<div class='ip-label'>Live arenas registered:</div>
							<div class='ip-desc'>${fn:length(competition.outstandingArenas) + 1}</div>
						</div>
					</div>
				</c:if>
				<div class='button-panel'>
					<c:if test="${user.tutor}">
						<button type='button' class='flat' onclick="location.href='./${competition.id}/'">Details</button>
					</c:if>
					<c:if test="${not competition.calculated}">
						<button type='button' class='flat' onclick="location.href='./${competition.id}/myPlayers/'">My Players</button>
					</c:if>
					<button type='button' class='flat' onclick="location.href='./view/${competition.id}/'">Competition Page</button>
					<c:if test="${user.instructor}">
						<button type='button' class='flat' onclick="$(this).toggle().next().toggle()">Delete</button>
						<button type='button' style="display:none;" onclick="location.href='./delete/${competition.id}/'" onmouseout="$(this).toggle().prev().toggle();">Confirm</button>
					</c:if>
				</div>
			</div>
		</c:if>
	</c:forEach>
</div>
				
<c:if test="${user.instructor}">

	<button id="newPopup" class='floating plus'></button>
	
	<div id="newCompetition" class="popup">
		<span class="button bClose">
			<span><b>X</b></span>
		</span>
		<h1> New Competition </h1>
		<form:form commandName="newCompetitionModel" enctype="multipart/form-data" method="POST">
			<div class='pasta-form narrow part'>
				<div class='pf-item one-col'>
					<div class='pf-label'>Name</div>
					<div class='pf-input'>
						<form:errors path="name" element="div" />
						<form:input autocomplete="off" type="text" path="name" />
					</div>
				</div>
				<div class='pf-item one-col'>
					<div class='pf-label'>Type</div>
					<div class='pf-input'>
						<form:errors path="type" element="div" />
						<form:select path="type">
							<option value="calculated">Calculated</option>
							<option value="arena">Arena</option>
						</form:select> 
					</div>
				</div>
				<div class='button-panel'>
					<button type="submit" id="submit">Create</button>
				</div>
			</div>
		</form:form>
	</div>
	
		
	<script>
        $(function() {
            $('#newPopup').on('click', function(e) {
                e.preventDefault();
                $('.popup').bPopup();
            });
            
            $("#newCompetition select").chosen({width: "100%", disable_search:true});
            
	        <spring:hasBindErrors name='newCompetitionModel'>
				$('.popup').bPopup();
	    	</spring:hasBindErrors>
        });
	</script>
</c:if>
