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
<%@ taglib prefix="pasta" uri="pastaTag"%>

<h1><c:out value='${competition.calculated ? "Calculated" : "Arena"}' /> Competition - ${competition.name}</h1>

<form:form commandName="updateCompetitionForm" enctype="multipart/form-data" method="POST">
	<div class='section'>
		<h2 class='section-title'>Details</h2>
		<div class='part'>
			<div class='pasta-form'>
				<div class='pf-section'>
					<div class='pf-item one-col'>
						<div class='pf-label'>Competition Name:</div>
						<div class='pf-input'>
							<form:errors path="name" element="div"/>
							<form:input path="name"/>
						</div>
					</div>
					<div class='pf-item one-col'>
						<label><form:checkbox path="hidden" />Hidden competition</label>
					</div>
					<div class='pf-horizontal two-col'>
						<div class='pf-item'>
							<div class='pf-label'>Is live:</div>
							<div class='pf-input'>
								<span class="pastaTF pastaTF${liveAssessmentCount gt 0}">${liveAssessmentCount gt 0}</span>
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Next run:</div>
							<div class='pf-input'>
								<c:choose>
									<c:when test="${empty competition.nextRunDate }">
										Never
									</c:when>
									<c:otherwise>
										<pasta:readableDate date="${competition.nextRunDate}" />
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>
					<div class='pf-item one-col'>
						<div class='pf-label'>First run:</div>
						<div class='pf-input'>
							<form:errors path="firstStartDateStr" element="div"/>
							<form:input path="firstStartDateStr"/>
						</div>
					</div>
					<div class='pf-item one-col'>
						<div class='pf-label'>Frequency:</div>
						<form:errors path="frequency" element="div"/>
					</div>
					<div class='pf-horizontal five-col'>
						<div class='pf-item'>
							<div class='pf-label'>Years</div>
							<div class='pf-input'>
								<form:input type="number" min="0" path="frequency.years" />
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Days</div>
							<div class='pf-input'>
								<form:input type="number" min="0" path="frequency.days" />
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Hours</div>
							<div class='pf-input'>
								<form:input type="number" min="0" path="frequency.hours" />
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Minutes</div>
							<div class='pf-input'>
								<form:input type="number" min="0" path="frequency.minutes" />
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Seconds</div>
							<div class='pf-input'>
								<form:input type="number" min="0" path="frequency.seconds" />
							</div>
						</div>
					</div>
				</div>
				<div class='pf-section'>
					<div class='pf-horizontal two-col'>
						<div class='pf-item'>
							<div class='pf-label'>Tutor arena permissions:</div>
							<div class='pf-input'>
								<form:errors path="tutorPermissions" element="div"/>
								<form:select path="tutorPermissions" cssClass="chosen-no-search"><form:options /></form:select>
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Student arena permissions:</div>
							<div class='pf-input'>
								<form:errors path="studentPermissions" element="div"/>
								<form:select path="studentPermissions" cssClass="chosen-no-search"><form:options /></form:select>
							</div>
						</div>
					</div>
				</div>
				<div class='pf-section'>
					<div class='pf-item one-col'>
						<div class='pf-label'>Upload code:</div>
						<div class='pf-input'><form:input type="file" path="file"/></div>
					</div>
					<c:if test="${competition.hasCode}">
						<div class='pf-item one-col'>
							<div class='pf-label'>Current code:</div>
							<div class='pf-input'>
								<jsp:include page="../../recursive/fileWriterRoot.jsp">
									<jsp:param name="owner" value="competition"/>
									<jsp:param name="fieldId" value="${competition.id}"/>
								</jsp:include>
							</div>
						</div>
					</c:if>
					<div class='pf-item one-col'>
						<div class='pf-label'>Has been tested:</div>
						<div class='pf-input'>
							<span class="pastaTF pastaTF${competition.tested}">${competition.tested}</span>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<c:if test="${competition.hasCode}">
		<div class='section'>
			<h2 class='section-title'>Run Options</h2>
			<div class='part'>
				<c:choose>
					<c:when test="${competition.calculated}">
						<div id='calculatedOptions' class='pasta-form'>
							<div class='pf-section'>
								<div class='pf-item one-col'>
									<label><form:checkbox path="hasBuild" />Custom build script</label>
								</div>
								<div class='pf-item one-col BuildOptionSet'>
									<div class='pf-label'>Timeout (ms):</div>
									<div class='pf-input'><form:input path="buildOptions.timeout"/></div>
								</div>
								<div class='pf-horizontal two-col BuildOptionSet'>
									<div class='pf-item'>
										<div class='pf-label'>Main script:</div>
										<div class='pf-input'><form:select path="buildOptions.scriptFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
									<div class='pf-item'>
										<div class='pf-label'>Input file:</div>
										<div class='pf-input'><form:select path="buildOptions.inputFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
								</div>
								<div class='pf-horizontal two-col BuildOptionSet'>
									<div class='pf-item'>
										<div class='pf-label'>Output file:</div>
										<div class='pf-input'><form:select path="buildOptions.outputFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
									<div class='pf-item'>
										<div class='pf-label'>Error file:</div>
										<div class='pf-input'><form:select path="buildOptions.errorFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
								</div>
							</div>
							<div class='pf-section'>
								<div class='pf-item one-col'>
									<label><form:checkbox path="hasRun" />Custom execute script</label>
								</div>
								<div class='pf-item one-col RunOptionSet'>
									<div class='pf-label'>Timeout (ms):</div>
									<div class='pf-input'><form:input path="buildOptions.timeout"/></div>
								</div>
								<div class='pf-horizontal two-col RunOptionSet'>
									<div class='pf-item'>
										<div class='pf-label'>Main script:</div>
										<div class='pf-input'><form:select path="runOptions.scriptFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
									<div class='pf-item'>
										<div class='pf-label'>Input file:</div>
										<div class='pf-input'><form:select path="runOptions.inputFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
								</div>
								<div class='pf-horizontal two-col RunOptionSet'>
									<div class='pf-item'>
										<div class='pf-label'>Output file:</div>
										<div class='pf-input'><form:select path="runOptions.outputFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
									<div class='pf-item'>
										<div class='pf-label'>Error file:</div>
										<div class='pf-input'><form:select path="runOptions.errorFilename" items="${codeFiles}" cssClass="chosen"/></div>
									</div>
								</div>
							</div>
						</div>
					</c:when>
					<c:otherwise>
						<div id='arenaOptions'>
							No special options.
						</div>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</c:if>
	
	<div class='section'>
		<div class='part button-panel'>
			<form:input type="hidden" path="id"/>
			<button type="submit" id="submit">Save Competition</button>
		</div>
	</div>

	<c:if test="${not competition.calculated}">
		<div class='section'>
			<h2 class='section-title'>Arena Details</h2>
			<div class='part'>
				<table id='areaDetails' class='display'>
					<thead>
						<tr><th>Name</th><th>First Run Date</th><th>Repeating Frequency</th><th>Password protected</th></tr>
					</thead>
					<tbody>
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
					</tbody>
				</table>
			</div>
		</div>
	</c:if>
</form:form>
	
<script>
	;(function($) {

         // DOM Ready
        $(function() {
        	
        	$('#firstStartDateStr').datetimepicker({ dateFormat: 'dd/mm/yy', timeFormat: 'hh:mm' });

        	$(".chosen").chosen({
        		width: "100%"
        	});
        	$(".chosen-no-search").chosen({
        		width: "100%",
        		disable_search: true
        	});
        	
        	<c:if test="${not competition.calculated}">
        	$("#areaDetails").DataTable();
			</c:if>
        	
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