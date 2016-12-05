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

<h1>Arena Details - ${competition.name}</h1>

<div class='section'>
	<h2 class='section-title'>Official Arena</h2>
	<div class='part'>
		<table id='officialArena'>
			<thead>
				<tr>
					<th>Name</th>
					<th>Number of Players</th>
					<th>Next Execution Date</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td><a href="${competition.officialArena.id}/">${competition.officialArena.name}</a></td>
					<td>${competition.officialArena.numPlayers}</td>
					<td>${competition.officialArena.nextRunDate}</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<div class='section'>
	<h2 class='section-title' class='display'>Unofficial Arenas</h2>
	<div class='part'>
		<table id='unofficialArenas'>
			<thead>
				<tr>
					<th>Name</th>
					<th>Number of Players</th>
					<th>Next Execution Date</th>
					<!--  <th>Password protected</th> -->
				</tr>
			</thead>
			<tbody>
				<c:forEach var="arena" items="${competition.outstandingArenas}">
					<tr>
						<td><a href="${arena.id}/">${arena.name}</a></td>
						<td>${arena.numPlayers}</td>
						<td>${arena.nextRunDate}</td>
						<!-- 
						<td><c:choose>
								<c:when test="${arena.passwordProtected}">
								YES 
							</c:when>
								<c:otherwise>
								NO
							</c:otherwise>
							</c:choose>
						</td>
						 -->
					</tr>
				</c:forEach>
				<c:forEach var="arena" items="${competition.completedArenas}">
					<tr>
						<td><a href="${arena.id}/">${arena.name}</a></td>
						<td>${arena.numPlayers}</td>
						<td>Completed</td>
						<!-- 
						<td><c:choose>
								<c:when test="${arena.passwordProtected}">
								YES 
							</c:when>
								<c:otherwise>
								NO
							</c:otherwise>
							</c:choose>
						</td>	
						 -->		
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<c:if test="${competition.studentCanCreateArena || user.tutor}">
	<button type='button' class='floating plus' id="newPopup"></button>
</c:if>



<c:if test="${competition.studentCanCreateArena || user.tutor}">

	<div id="newArena">
		<span class="button bClose"> <span><b>X</b></span>
		</span>
		<h1>New Arena</h1>
		<form:form commandName="newArenaModel" enctype="multipart/form-data"
			method="POST">
			<div class='pasta-form narrow part'>
				<div class='pf-item one-col'>
					<div class='pf-label'>Name</div>
					<div class='pf-input'>
						<form:errors path="name" element="div" />
						<form:input autocomplete="off" type="text" path="name" />
					</div>
				</div>
				<div class='pf-item one-col'>
					<div class='pf-label'>First run</div>
					<div class='pf-input'>
						<form:input path="firstStartDateStr" />
					</div>
				</div>
				<c:if
					test="${user.instructor || (user.tutor && competition.tutorCreatableRepeatableArena) || competition.studentCreatableRepeatableArena}">
					<div class='pf-item one-col'>
						<div class='pf-label'>Repeat every</div>
					</div>
					<div class='pf-horizontal five-col'>
						<c:if test="${user.tutor}">
							<div class='pf-item'>
								<div class='pf-label'>Years</div>
								<div class='pf-input'><form:input type="number" path="frequency.years" /></div>
							</div>
						</c:if>
						<div class='pf-item'>
							<div class='pf-label'>Days</div>
							<div class='pf-input'><form:input type="number" path="frequency.days" /></div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Hours</div>
							<div class='pf-input'><form:input type="number" path="frequency.hours" /></div>
						</div>
						<c:if test="${user.tutor}">
							<div class='pf-item'>
								<div class='pf-label'>Minutes</div>
								<div class='pf-input'><form:input type="number" path="frequency.minutes" /></div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Seconds</div>
								<div class='pf-input'><form:input type="number" path="frequency.seconds" /></div>
							</div>
						</c:if>
					</div>
				</c:if>
				<%--
				<div class='pf-item one-col'>
					<div class='pf-label'>Password</div>
					<div class='pf-input'>
						<form:input autocomplete="off" type="password" path="password" onkeyup="checkPasswords();" value="" />
					</div>
				</div>
				<div class='pf-item one-col'>
					<div class='pf-label'>Confirm password</div>
					<div class='pf-input'>
						<form:input autocomplete="off" type="password" onkeyup="checkPasswords();" value="" />
					</div>
				</div>
				--%>
				<div class='button-panel'>
					<button type="submit" id="submit">Create</button>
				</div>
			</div>
			<form:input type="hidden" path="competition.id" value="${competition.id}"/>
		</form:form>
	</div>

	<style>
.glowingRed {
	outline: none;
	border-color: #f00;
	box-shadow: 0 0 10px #f00;
}

.glowingGreen {
	outline: none;
	border-color: #0f0;
	box-shadow: 0 0 10px #0f0;
}
</style>

	<script>
		;
		(function($) {

			// DOM Ready
			$(function() {
				$( "#firstStartDateStr" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});

				$("#officialArena").DataTable({
					paging: false,
					ordering: false,
					searching: false,
					info: false
				});
				$("#unofficialArenas").DataTable({
					language: {
						emptyTable: "No unofficial arenas created"
					}
				});
				
				$('#newPopup').on('click', function(e) {
					$('#newArena').bPopup();
				});
			});

		})(jQuery);

		function checkPasswords() {
			var elements = $("input:password");
			if (elements[0].value == elements[1].value) {
				document.getElementById("submit").disabled = false;
				$("input:password").addClass("glowingGreen");
				$("input:password").removeClass("glowingRed");
			} else {
				document.getElementById("submit").disabled = true;
				$("input:password").addClass("glowingRed");
				$("input:password").removeClass("glowingGreen");
			}
		}
	</script>

</c:if>
