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

<h1 style="margin-bottom:0.5em;">${assessment.name}</h1>

<spring:hasBindErrors name="updateAssessmentForm">
	<div class="ui-state-error ui-corner-all vertical-block" style="font-size: 1.5em;">
		<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
		<b>ERROR: Please check your form for indicated errors, and try again.</b>
	</div>
</spring:hasBindErrors>

<div id='loading' class='align-contents-middle'>
	<div class='horizontal-block'>
		<img src="<c:url value='/static/images/ajax-loader.gif'/>" />
	</div>
	<div class='horizontal-block'>
		<p>Loading assessment...
	</div>
</div>
<div id='hideUntilLoaded' style='display:none'>
	<c:if test="${not assessment.completelyTested}" >
		<div class="ui-state-error ui-corner-all vertical-block" style="font-size: 1.5em;">
			<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span><b>WARNING:</b> This assessment contains untested unit tests
		</div>
	</c:if>
	
	<button style="margin-top:1em;float:left;" class="button" onclick="window.location.href=window.location.href+'run/'">Re-Run Assessment</button>
	
	<form:form commandName="updateAssessmentForm" enctype="multipart/form-data" method="POST">
		<form:input type="hidden" path="id" value="${assessment.id}"/>
		<input type="submit" value="Save Assessment" id="submit" style="margin-top:1em;"/>
		
		<table class='alignCellsTop noGaps'>
			<tr>
				<td>Name:</td>
				<td>
					<form:input type="text" path="name"/>
					<form:errors path="name"/>
				</td>
			</tr>
			<tr>
				<td>Category: <span class='help'>Separate multiple categories with commas.</span></td>
				<td>
					<form:input type="text" path="category"/>
					<form:errors path="category"/>
				</td>
			</tr>
			<tr>
				<td>Assessment marks:</td>
				<td>
					<form:input type="text" path="marks"/>
					<form:errors path="marks"/>
				</td>
			</tr>
			<tr class='spaceBelow'>
				<td>Assessment due date:</td>
				<td>
					<form:input type="text" path="strDate" />
					<form:errors path="dueDate"/>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<label>
						<input type="checkbox" id='toggleGroups' <c:if test="${assessment.groupWork}">checked='checked'</c:if> />
						Allow group work
					</label>
				</td>
			</tr>
			<tr class='groupData'>
				<td>Number of groups:</td>
				<td>
					<form:input type="number" min="0" path="groupCount" />
					<label><input type="checkbox" id='unlimitedGroups' <c:if test="${assessment.groupCount == -1}">checked='checked'</c:if> />No limit</label>
					<form:errors path="groupCount"/>
				</td>
			</tr>
			<tr class='groupData'>
				<td>Maximum group size:</td>
				<td>
					<form:input type="number" min="2" path="groupSize" />
					<label><input type="checkbox" id='unlimitedSize' <c:if test="${assessment.groupSize == -1}">checked='checked'</c:if> />No limit</label>
					<form:errors path="groupSize"/>
				</td>
			</tr>
			<tr class='groupData'>
				<td>Groups finalised:</td>
				<td>
					<form:input type="text" path="strGroupLock" />
					<span class='help'>The date when students will no longer be able to move between groups. Tutors will still be able to assign groups after this date.</span>
					<form:errors path="groupLockDate"/>
				</td>
			</tr>
			<tr class='spaceBelow groupData'>
				<td><label for="studentsManageGroups1">Allow students to manage group membership:</label></td>
				<td><form:checkbox path="studentsManageGroups"/></td>
			</tr>
			<tr>
				<td>Maximum number of allowed submissions:</td>
				<td>
					<form:input type="text" path="numSubmissionsAllowed"/>
					<form:errors path="numSubmissionsAllowed"/>
				</td>
			</tr>
			<tr class='spaceBelow'>
				<td><label for="countUncompilable1">Count submissions that have failed to compile:</label></td>
				<td><form:checkbox path="countUncompilable"/></td>
			</tr>
			<tr>
				<td>
					Solution name:
					<span class='help'>The name of the main solution source code file. <strong>Only required if you use Black Box unit tests.</strong> If students are to submit <code>MyProgram.java</code> and <code>MyProgram.c</code>, then solution name should be "MyProgram"</span>
				</td>
				<td>
					<form:input path="solutionName"/>
					<form:errors path="solutionName"/>
				</td>
			</tr>
			<tr class='spaceBelow'>
				<td>Allowed languages:</td>
				<td>
					<form:errors path="languages" element="div"/>
					<form:select path="languages" multiple="multiple" cssClass="langSelect">
						<form:options items="${allLanguages}" itemLabel="description"/>
					</form:select>
				</td>
			</tr>
			<tr>
				<td>
					Upload custom validator:
					<a href="../../help/customValidation/" target="_blank"><span class='help'>Click here for information about custom validators.</span></a>
				</td>
				<td>
					<form:input type="file" path="validatorFile"/> <form:errors path="validatorFile"/>
				</td>
			</tr>
			<c:if test="${assessment.customValidator}">
				<tr>
					<td>Current validator:</td>
					<td>
						<jsp:include page="../../recursive/fileWriterRoot.jsp">
							<jsp:param name="owner" value="Validator - ${assessment.name}"/>
						</jsp:include>
					</td>
				</tr>
			</c:if>
		</table>
		
		<h2>Release Rule</h2>
		<div>
			<div class="vertical-block">
				<p><strong>Current release rule:</strong>
				<p>${assessment.releaseDescription}
			</div>
			<div class="vertical-block">
				<c:set var="buttonText" value="Set" />
				<c:if test="${assessment.released}"><c:set var="buttonText" value="Modify" /></c:if>
				<a href="../release/${assessment.id}/"><c:out value="${buttonText}" /> Release Rule</a>
			</div>		
		</div>
		
		
		<h2>Description</h2>
		<div id='descriptionHTML'>
			${assessment.description}
		</div>
		<button type="button" id="modifyDescription">Modify Description</button>
		<div style="display:none">
			<form:textarea path="description" cols="110" rows="10" /><br/>
		</div>
		
		<div class='vertical-block'>
			<h2>Unit Tests</h2>
			<form:errors path="selectedUnitTests" element="p" />
			<table class='moduleTable'>
				<thead>
					<tr><th>Selected</th><th>Name</th><th>Weighting</th><th>Secret</th><th>Group Work</th><th>Tested</th></tr>
				</thead>
				<tbody>
					<c:forEach var='module' items="${allUnitTests}" varStatus="index">
						<tr>
							<form:input type="hidden" path="selectedUnitTests[${index.index}].id" value="${module.id}"/>
							<form:input type="hidden" path="selectedUnitTests[${index.index}].test.id" value="${module.test.id}"/>
							<td>
								<input class='custom-check select-check' id="selectedUnitTests${index.index}.selected" type='checkbox' <c:if test="${module.id != 0 or module.weight != 0 or module.groupWork}">checked="checked"</c:if> />
								<label for="selectedUnitTests${index.index}.selected"></label>
							</td>
							<td>
								<a href="../../unitTest/${module.test.id}/">${module.test.name}</a>
							</td>
							<td>
								<form:input size="5" type="text" path="selectedUnitTests[${index.index}].weight" value="${module.weight}"/>
							</td>
							<td>
								<form:checkbox cssClass="custom-check" path="selectedUnitTests[${index.index}].secret" checked="${module.secret ? 'checked' : ''}"  />
								<label for="selectedUnitTests${index.index}.secret1"></label>
							</td>
							<td>
								<form:checkbox cssClass="custom-check" path="selectedUnitTests[${index.index}].groupWork" checked="${module.groupWork ? 'checked' : ''}"  />
								<label for="selectedUnitTests${index.index}.groupWork1"></label>
							</td>
							<td class="pastaTF pastaTF${module.test.tested}">
								${module.test.tested}
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
		
		<div class='vertical-block'>
			<h2>Hand Marking</h2>
			<form:errors path="selectedHandMarking" element="p" />
			<table class='moduleTable'>
				<thead>
					<tr><th>Selected</th><th>Name</th><th>Weighting</th><th>Group Work</th></tr>
				</thead>
				<tbody>
					<c:forEach var='module' items="${allHandMarking}" varStatus="index">
						<tr>
							<form:input type="hidden" path="selectedHandMarking[${index.index}].id" value="${module.id}"/>
							<form:input type="hidden" path="selectedHandMarking[${index.index}].handMarking.id" value="${module.handMarking.id}"/>
							<td>
								<input class='custom-check select-check' id="selectedHandMarking${index.index}.selected" type='checkbox' <c:if test="${module.id != 0 or module.weight != 0 or module.groupWork}">checked="checked"</c:if> />
								<label for="selectedHandMarking${index.index}.selected"></label>
							</td>
							<td>
								<a href="../../handMarking/${module.handMarking.id}/">${module.handMarking.name}</a>
							</td>
							<td>
								<form:input size="5" type="text" path="selectedHandMarking[${index.index}].weight" value="${module.weight}"/>
							</td>
							<td>
								<form:checkbox cssClass="custom-check" path="selectedHandMarking[${index.index}].groupWork" checked="${module.groupWork ? 'checked' : ''}"  />
								<label for="selectedHandMarking${index.index}.groupWork1"></label>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
		
		<div class='vertical-block'>
			<h2>Competitions</h2>
			<form:errors path="selectedCompetitions" element="p" />
			<table class='moduleTable'>
				<thead>
					<tr><th>Selected</th><th>Name</th><th>Weighting</th><th>Group Work</th></tr>
				</thead>
				<tbody>
					<c:forEach var='module' items="${allCompetitions}" varStatus="index">
						<tr>
							<form:input type="hidden" path="selectedCompetitions[${index.index}].id" value="${module.id}"/>
							<form:input type="hidden" path="selectedCompetitions[${index.index}].competition.id" value="${module.competition.id}"/>
							<td>
								<input class='custom-check select-check' id="selectedCompetitions${index.index}.selected" type='checkbox' <c:if test="${module.id != 0 or module.weight != 0 or module.groupWork}">checked="checked"</c:if> />
								<label for="selectedCompetitions${index.index}.selected"></label>
							</td>
							<td>
								<a href="../../competition/${module.competition.id}/">${module.competition.name}</a>
							</td>
							<td>
								<form:input size="5" type="text" path="selectedCompetitions[${index.index}].weight" value="${module.weight}"/>
							</td>
							<td>
								<form:checkbox cssClass="custom-check" path="selectedCompetitions[${index.index}].groupWork" checked="${module.groupWork ? 'checked' : ''}"  />
								<label for="selectedCompetitions${index.index}.groupWork1"></label>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
		
		<input type="submit" value="Save Assessment" id="submit"/>
	</form:form>
</div>

<script>
    $(function() {
    	$( "#strDate" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	$( "#strGroupLock" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	
    	$( ".moduleTable input:text" ).each(function() {
    		$textBox = $(this);
    		$textBox.tipsy({trigger: 'focus', gravity: 'n', title: function() {
    			if(!$(this).parents("tr").is(".selected")) {
    				return "Don't forget to select the row.";
    			}
    			return "";
    		}});
    	});
    	
    	$(".langSelect").chosen({
    		placeholder_text_multiple: "Leave blank to allow ANY language", 
    		width: "22em"
    	});
    	
    	$("#toggleGroups").on("change", function() {
    		var groupWork = $(this).is(":checked");
    		$("tr.groupData").toggle(groupWork);
    		$(this).parents("tr").toggleClass("spaceBelow", !groupWork)
    		if(!groupWork) {
    			$("#groupCount").val(0);
    		}
    	});
    	$("#unlimitedGroups").on("change", function() {
    		$("#groupCount").prop("disabled", $(this).is(":checked"));
    		$("#groupCount").val($(this).is(":checked") ? -1 : $("#groupCount").val() == -1 ? 0 : $("#groupCount").val());
    	});
    	$("#unlimitedSize").on("change", function() {
    		$("#groupSize").prop("disabled", $(this).is(":checked"));
    		$("#groupSize").val($(this).is(":checked") ? -1 : $("#groupSize").val() == -1 ? 2 : $("#groupSize").val());
    	});
    	$('#updateAssessmentForm').on('submit', function() {
    		$("#groupCount").removeAttr('disabled');
    		$("#groupSize").removeAttr('disabled');
        });
    	$("#toggleGroups,#unlimitedGroups,#unlimitedSize").trigger("change");
    	
        
        
        $("#modifyDescription").bind('click', function(e) {
        	$("#description").parents('div').show();
        	$("#modifyDescription").hide();
        });
        
        var timer;
        $("#description").wysiwyg({
        	initialContent: function() {
    			return "";
    		},
        	controls: {
        		html  : { visible: true }
        	},
        	events: {
        		keyup: function() {
        			clearTimeout(timer);
                	timer = setTimeout(function() {
                		$("#descriptionHTML").html($("#description").val());
                	}, 1000);
        		}
        	}
        });
        
        // When you click on a selected checkbox, mark the row as selected
        $("input.select-check").on('click', function() {
        	$(this).parents("tr").toggleClass("selected", $(this).is(":checked"));
        	refreshTable($(this).parents("table.moduleTable"));
        });
        
        // When submitting, ignore non-selected module rows
        $("#updateAssessmentForm").on("submit", function() {
        	$("input.select-check").parents("tr").not(".selected").find("input").prop("disabled", true);
        });
        
    	var i = 0;
        // Initialise module tables
		$.fn.dataTable.ext.order['dom-checkbox'] = function(settings, col) {
			return this.api().column(col, {order : 'index'}).nodes().map(function(td, i) {
				return $('input', td).prop('checked') ? '0' : '1';
			});
		};
		$("input.select-check:checked").parents("tr").addClass("selected");
		$("table.moduleTable").each(function() {
			$(this).addClass("compact");
			$(this).parent().width("60em");
			$(this).attr("width", "100%");
			$(this).DataTable({
				deferDrawing : true,
				paging : true,
				info : true,
				searching : true,
				ordering : true,
				columnDefs : [ {
					targets : 0,
					orderData : [0, 1],
					orderDataType : "dom-checkbox"
				}, {
					orderable : false,
					targets : "_all"
				} ],
				language : {
					emptyTable : "None available."
				}
			});
			refreshTable($(this));
		});

		function refreshTable($table) {
			$table.DataTable().draw(false);
			$rows = $('tbody>tr', $table);
			$table.parent().find('a.paginate_button').filter(function() {
				return $(this).text() === '1';
			}).effect('highlight', {}, 1500);
			$rows.removeClass("lastSelected").filter(".selected").last()
					.addClass("lastSelected");
		}

		$("#hideUntilLoaded").show();
		$("#loading").hide();
	});
</script>
