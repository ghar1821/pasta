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
		<td><a href="${competition.officialArena.shortName}/">${competition.officialArena.name}</a></td>
		<td>${competition.officialArena.numPlayers}</td>
		<td>${competition.officialArena.nextRunDate}</td>
	</tr>
</table>

<h1>Unofficial Arenas</h1>

<c:if test="${competition.studentCreatableArena || unikey.tutor}">
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
			<td><a href="${arena.shortName}/">${arena.name}</a></td>
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
			<td><a href="${arena.shortName}/">${arena.name}</a></td>
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

<c:if test="${competition.studentCreatableArena || unikey.tutor}">

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
