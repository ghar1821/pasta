<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h1>Assessments</h1>

<table style="width: 100%;">
	<c:forEach var="assessment" items="${allAssessments}">
		<tr>
			<!-- icons -->
			<td style="width: 5em"><c:if
					test="${not assessment.completelyTested}">
					<span class="ui-icon ui-icon-alert"
						style="float: left; margin-right: .3em;"
						title="Contains untested unit tests."></span>
				</c:if> <c:if test="${assessment.closed}">
					<span class="ui-icon ui-icon-locked"
						style="float: left; margin-right: .3em;" title="Past due date"></span>
				</c:if> <c:if test="${not assessment.released}">
					<!--  <span class="ui-icon ui-icon-gear" style="float: left; margin-right: .3em;" title="Not released"></span>-->
					<button id="release"
						onClick="javascript:toggle('release${assessment.shortName}');">
						<span class="ui-icon ui-icon-gear" title="Not released"></span>
					</button>
					<!--  onClick="document.getElementById('comfirmButton').onclick = function(){ location.href='./release/${assessment.shortName}/'};$('#comfirmPopup').bPopup();">-->
					<div style="clear: both;"></div>
					<div id="release${assessment.shortName}" style="display: none;">
						<ul class="tristate list">
							<li class="list"><input type="checkbox"> All <c:forEach
									var="stream" items="${allStream}">
									<c:forEach var="class" items="${allClass}">
									</c:forEach>
								</c:forEach>
								<ul>
									<li class="list"><input type="checkbox"><a href="#">Solutions</a>
										<ul>
											<li class="list"><input type="checkbox"><a href="#">Government</a></li>
											<li class="list"><input type="checkbox"><a href="#">Manufacturing</a></li>
											<li class="list"><input type="checkbox"><a href="#">Solutions</a>
												<ul>
													<li class="list"><input type="checkbox" checked><a href="#">Consumer
															photo and video</a></li>
													<li class="list"><input type="checkbox" checked><a href="#">Mobile</a></li>
													<li class="list"><input type="checkbox"><a href="#">Rich
															Internet applications</a></li>
													<li class="list"><input type="checkbox"><a href="#">Technical
															communication</a></li>
													<li class="list"><input type="checkbox"><a href="#">Training
															and eLearning</a></li>
													<li class="list"><input type="checkbox"><a href="#">Web
															conferencing</a></li>
												</ul></li>
											<li class="list"><input type="checkbox"><a href="#">All
													industries and solutions</a></li>
										</ul></li>
								</ul></li>
						</ul>
					</div>
					<!-- just added -->
				</c:if></td>
			<!-- Data -->
			<td><b>${assessment.name}</b> - <c:choose>
					<c:when test="${assessment.marks != 0}">
Out of ${assessment.marks}
</c:when>
					<c:otherwise>
Unmarked
</c:otherwise>
				</c:choose> <br /> ${assessment.dueDate} <br /> <c:choose>
					<c:when test="${assessment.numSubmissionsAllowed == 0}">
&infin;
</c:when>
					<c:otherwise>
${assessment.numSubmissionsAllowed}
</c:otherwise>
				</c:choose> sumbissions allowed <br /> <c:choose>
					<c:when
						test="${(fn:length(assessment.unitTests) + fn:length(assessment.secretUnitTests) + fn:length(assessment.handMarking) + fn:length(assessment.competitions)) == 0}">
No tests
</c:when>
					<c:otherwise>
						<c:if test="${fn:length(assessment.unitTests) > 0}">
${fn:length(assessment.unitTests)} Unit Tests </br>
						</c:if>
						<c:if test="${fn:length(assessment.secretUnitTests) > 0}">
${fn:length(assessment.secretUnitTests)} Secret Unit Tests </br>
						</c:if>
						<c:if test="${fn:length(assessment.handMarking) > 0}">
${fn:length(assessment.handMarking)} Hand marking templates </br>
						</c:if>
						<c:if test="${fn:length(assessment.competitions) > 0}">
${fn:length(assessment.competitions)} Competitions </br>
						</c:if>
					</c:otherwise>
				</c:choose></td>
			<td>
				<div style="float: left">
					<button style="float: left; text-align: center;"
						onclick="location.href='./${assessment.shortName}/'">Details</button>
				</div>
				<div style="float: left">
					<button style="float: left; text-align: center;"
						onclick="location.href='./stats/${assessment.shortName}/'">Statistics</button>
				</div>
				<div style="float: left">
					<button style="float: left; text-align: center;"
						onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
					<button style="float: left; display: none; text-align: center;"
						onclick="location.href='./delete/${assessment.shortName}/'"
						onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
				</div> <c:if
					test="${(fn:length(assessment.unitTests) + fn:length(assessment.secretUnitTests)) != 0}">
					<div style="float: left">
						<button style="float: left; text-align: center;"
							onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Re-run</button>
						<button style="float: left; display: none; text-align: center;"
							onclick="location.href='./${assessment.shortName}/run/'"
							onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
					</div>
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table>

<button id="newPopup">Add a new Assessment</button>

<div id="newAssessment">
	<span class="button bClose"> <span><b>X</b></span>
	</span>
	<h1>New Assessment</h1>
	<form:form commandName="assessment" enctype="multipart/form-data"
		method="POST">
		<table>
			<tr>
				<td>Assessment Name:</td>
				<td><form:input autocomplete="off" type="text" path="name"
						value="" /></td>
			</tr>
			<tr>
				<td>Assessment Marks:</td>
				<td><form:input type="text" path="marks" /></td>
			</tr>
			<tr>
				<td>Assessment DueDate:</td>
				<td><form:input type="text" path="simpleDueDate"
						id="simpleDueDate" name="simpleDueDate" /></td>
			</tr>
			<tr>
				<td>Maximum Number of allowed submissions:</td>
				<td><form:input type="text" path="numSubmissionsAllowed" /></td>
			</tr>
		</table>
		<input type="submit" value="Create" id="submit" />
	</form:form>
</div>

<div id="released">
	<span class="button bClose"> <span><b>X</b></span>
	</span>
	<h1>Release</h1>
	<form:form commandName="assessment" enctype="multipart/form-data"
		method="POST">
		<c:set var="rel" value="1" />
		<table>
			<tr>
				<td>Assessment Name:</td>
				<td>${assessment.name}</td>
				<!--<form:input autocomplete="off" type="text" path="name" value=""/></td>-->
			</tr>
			<tr>
				<td>
					<ul class=tristate">
						<li item-selected='false'>All</li>
						<li item-checked='true' item-expanded='true'><a href="#">Solutions</a>
							<ul>
								<li><a href="#">Government</a></li>
								<li item-checked='false'><a href="#">Manufacturing</a></li>
								<li><a href="#">Solutions</a>
									<ul>
										<li><a href="#">Consumer photo and video</a></li>
										<li><a href="#">Mobile</a></li>
										<li><a href="#">Rich Internet applications</a></li>
										<li><a href="#">Technical communication</a></li>
										<li><a href="#">Training and eLearning</a></li>
										<li><a href="#">Web conferencing</a></li>
									</ul></li>
								<li><a href="#">All industries and solutions</a></li>
							</ul></li>
					</ul>
				</td>
				<td>For</td>
				<td><input id="All" type="radio" name="Releasetype" value="All">All<br>
					<input id="Stream" type="radio" name="Releasetype" value="Stream ">Stream<br>
					<input id="Class" type="radio" name="Releasetype" value="Class ">Class
				</td>
			</tr>
			<tr>
				<c:choose>
					<c:when test="${rel == 1}">
					</c:when>


					<c:when test="${rel == 2 }">
						<tr>
							<td>Stream:</td>
							<td><form:input type="text" path="simpleDueDate"
									id="simpleDueDate" name="simpleDueDate" /></td>
						</tr>
					</c:when>
					<c:when test="${rel == 3}">
						<tr>
							<td>Class:</td>
							<td><form:input type="text" path="numSubmissionsAllowed" /></td>
						</tr>

					</c:when>
					<c:otherwise>

					</c:otherwise>
				</c:choose>
		</table>
		<input type="submit" value="Release" id="submit" />
	</form:form>
</div>

<div id="comfirmPopup">
	<span class="button bClose"> <span><b>X</b></span>
	</span>
	<h1>Are you sure you want to do that?</h1>
	<button id="comfirmButton" onClick="">Confirm</button>
</div>

<script>
	;
	(function($) {

		// DOM Ready
		$(function() {

			$("#simpleDueDate").datetimepicker({
				timeformat : 'hh:mm',
				dateFormat : 'dd/mm/yy'
			});// TODO

			// Binding a click event
			// From jQuery v.1.7.0 use .on() instead of .bind()
			$('#newPopup').bind('click', function(e) {

				// Prevents the default action to be triggered. 
				e.preventDefault();

				// Triggering bPopup when click event is fired
				$('#newAssessment').bPopup();

			});

		});

		$('ul.tristate').tristate();
	})(jQuery);

	function toggle(showHideDiv) {
		var ele = document.getElementById(showHideDiv);
		if (ele.style.display == "block") {
			ele.style.display = "none"
		} else {
			ele.style.display = "block";
		}
	}
</script>
<!--
