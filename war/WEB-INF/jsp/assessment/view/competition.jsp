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

<h1 style="margin-bottom:0.5em;"><c:out value='${competition.calculated ? "Calculated" : "Arena"}' /> Competition</h1>

<form:form commandName="updateCompetitionForm" enctype="multipart/form-data" method="POST">
	<table>
		<tr><td>Competition Name:</td><td><form:input path="name"/> <form:errors path="name"/></td></tr>
		<tr><td>Is live:</td><td class="pastaTF pastaTF${liveAssessmentCount gt 0}">${liveAssessmentCount gt 0}</td></tr>
		<tr><td colspan='2'><label><form:checkbox path="hidden" />Hidden competition</label></td></tr>
		
		<tr><td>First run:</td><td><form:input path="firstStartDateStr"/> <form:errors path="firstStartDateStr"/></td></tr>
		<tr>
			<td>Frequency:</td>
			<td>
				<form:input type="number" min="0" path="frequency.years" style="width:3em;"/> years
				<form:input type="number" min="0" path="frequency.days" style="width:3em;"/> days
				<form:input type="number" min="0" path="frequency.hours" style="width:3em;"/> hours
				<form:input type="number" min="0" path="frequency.minutes" style="width:3em;"/> minutes
				<form:input type="number" min="0" path="frequency.seconds" style="width:3em;"/> seconds
				<form:errors path="frequency"/>
			</td>
		</tr>
		<tr><td>Next Run:</td>
			<c:choose>
				<c:when test="${empty competition.nextRunDate }">
					<td>Never</td>
				</c:when>
				<c:otherwise>
					<td>${competition.nextRunDate}</td>
				</c:otherwise>
			</c:choose>
		</tr>
		
		<c:if test="${not competition.calculated}">
			<!-- can tutors make repeatable arenas -->
			<tr><td>Tutor arena permissions:</td>
			<td><form:select path="tutorPermissions"><form:options /></form:select> <form:errors path="tutorPermissions"/></td></tr>
			<!-- can students make arenas -->
			<tr><td>Student arena permissions:</td>
			<td><form:select path="studentPermissions"><form:options /></form:select> <form:errors path="studentPermissions"/></td></tr>
		</c:if>
	</table> 
	
	<div>
		<table>
			<tr><td>Upload Code:</td><td><form:input type="file" path="file"/></td></tr>
			<c:if test="${competition.hasCode}">
			<tr>
				<td>Current Code:</td>
				<td>
					<jsp:include page="../../recursive/fileWriterRoot.jsp"/>
				</td>
			</tr>
			</c:if>
			<tr><td>Has been tested:</td><td class="pastaTF pastaTF${competition.tested}">${competition.tested}</td></tr>
		</table>
		
		<c:if test="${competition.hasCode}">
		Run Options:
		<c:choose>
			<c:when test="${competition.calculated}">
				<div id='calculatedOptions'>
					<label><form:checkbox path="hasBuild" />Custom build script</label><br/>
					<div class='hiddenToStart optionset BuildOptionSet'>
						<table>
							<tr><td>Main script:</td><td><form:select path="buildOptions.scriptFilename" items="${codeFiles}"/></td></tr>
							<tr><td>Timeout:</td><td><form:input path="buildOptions.timeout"/></td></tr>
							<tr><td>Input file:</td><td><form:select path="buildOptions.inputFilename" items="${codeFiles}"/></td></tr>
							<tr><td>Output file:</td><td><form:select path="buildOptions.outputFilename" items="${codeFiles}"/></td></tr>
							<tr><td>Error file:</td><td><form:select path="buildOptions.errorFilename" items="${codeFiles}"/></td></tr>
						</table>
					</div>
					<label><form:checkbox path="hasRun" />Custom execute script</label><br/>
					<div class='hiddenToStart optionset RunOptionSet'>
						<table>
							<tr><td>Main script:</td><td><form:select path="runOptions.scriptFilename" items="${codeFiles}"/></td></tr>
							<tr><td>Timeout:</td><td><form:input path="runOptions.timeout"/></td></tr>
							<tr><td>Input file:</td><td><form:select path="runOptions.inputFilename" items="${codeFiles}"/></td></tr>
							<tr><td>Output file:</td><td><form:select path="runOptions.outputFilename" items="${codeFiles}"/></td></tr>
							<tr><td>Error file:</td><td><form:select path="runOptions.errorFilename" items="${codeFiles}"/></td></tr>
						</table>
					</div>
				</div>
			</c:when>
			<c:otherwise>
				<div id='arenaOptions'>
					No special options.
				</div>
			</c:otherwise>
		</c:choose>
		</c:if>
	</div>
	
	<form:input type="hidden" path="id"/>
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

	
<script>
	;(function($) {

         // DOM Ready
        $(function() {
        	
        	$('#firstStartDateStr').datetimepicker({ dateFormat: 'dd/mm/yy', timeFormat: 'hh:mm' });

        	<c:if test="${competition.calculated}">
			$('[id^=hasBuild]').on('change', function() {
				toggleEnabledOptions("Build");
			});
			$('[id^=hasRun]').on('change', function() {
				toggleEnabledOptions("Run");
			});
			
			toggleEnabledOptions("Build");
			toggleEnabledOptions("Run");
			</c:if>
        });

    })(jQuery);
	
	<c:if test="${competition.calculated}">
	function toggleEnabledOptions(types) {
		$('.' + types + 'OptionSet input').prop('disabled', !$('[id^=has' + types + ']').is(':checked'));
		if($('[id^=has' + types + ']').is(':checked')) {
			$('.' + types + 'OptionSet').show(300);
			$('.' + types + 'OptionSet').removeClass('hidden');
		} else {
			$('.' + types + 'OptionSet').hide(300);
		}
	}
	</c:if>
</script>