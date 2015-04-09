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

<h1>Official Arena</h1>
<table style="width: 100%;">
	<tr>
		<th>Name</th>
		<th>Number of Players</th>
		<th>Next Execution Date</th>
	</tr>
	<tr>
		<td><a href="${competition.officialArena.id}/">${competition.officialArena.name}</a></td>
		<td>${competition.officialArena.numPlayers}</td>
		<td>${competition.officialArena.nextRunDate}</td>
	</tr>
</table>

<h1>Unofficial Arenas</h1>

<c:if test="${competition.studentCanCreateArena || unikey.tutor}">
	<div style="float: left">
		<button style="float: left; text-align: center;" id="newPopup">Add
			Arena</button>
	</div>
</c:if>

<table style="width: 100%;">
	<tr>
		<th>Name</th>
		<th>Number of Players</th>
		<th>Next Execution Date</th>
		<!--  <th>Password protected</th> -->
	</tr>
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
</table>

<c:if test="${competition.studentCanCreateArena || unikey.tutor}">

	<div id="newArena">
		<span class="button bClose"> <span><b>X</b></span>
		</span>
		<h1>New Arena</h1>
		<form:form commandName="newArenaModel" enctype="multipart/form-data"
			method="POST">
			<table>
				<tr>
					<td>Arena Name:</td>
					<td><form:input autocomplete="off" type="text" path="name"
							value="" /></td>
				</tr>
				<tr>
					<td>First run:</td>
					<td><form:input path="firstStartDateStr" /></td>
				</tr>
				<c:if
					test="${unikey.instructor || (unikey.tutor && competition.tutorCreatableRepeatableArena) || competition.studentCreatableRepeatableArena}">
					<tr>
						<td>Repeat every:</td>
						<td>
							<c:if test="${unikey.tutor}">
								<form:input type="number" path="frequency.years" style="width:3em;" /> years 
							</c:if>
							<form:input type="number" path="frequency.days" style="width:3em;" /> days 
							<form:input	type="number" path="frequency.hours" style="width:3em;" /> hours
							
							<c:if test="${unikey.tutor}">
								<form:input type="number" path="frequency.minutes" style="width:3em;" /> minutes 
								<form:input type="number" path="frequency.seconds" style="width:3em;" /> seconds
							</c:if>
						</td>
					</tr>
				</c:if>
				<!-- 
				<tr>
					<td>Password:</td>
					<td><input autocomplete="off" type="password" path="password"
						onkeyup="checkPasswords();" value="" /></td>
				</tr>
				<tr>
					<td>Confirm Password:</td>
					<td><input autocomplete="off" type="password"
						onkeyup="checkPasswords();" value="" /></td>
				</tr>
				 -->
			</table>
			<form:input type="hidden" path="competition.id" value="${competition.id}"/>
			<button type="submit" id="submit">Create</button>
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

				// Binding a click event
				// From jQuery v.1.7.0 use .on() instead of .bind()
				$('#newPopup').bind('click', function(e) {

					// Prevents the default action to be triggered. 
					e.preventDefault();

					// Triggering bPopup when click event is fired
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
