<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pasta" uri="pastaTag"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

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
	
	<div class='section'>
		<div class='button-panel'>
			<button class='flat' onclick="location.href=window.location.href+'run/'">Re-Run Assessment</button>
		</div>
	</div>
	
	<form:form commandName="updateAssessmentForm" enctype="multipart/form-data" method="POST">
		<form:input type="hidden" path="id" value="${assessment.id}"/>
		
		<div class='section'>
			<h2 class='section-title'>Details</h2>
			<div class='part'>
				<div class='pasta-form'>
					<div class='pf-section'>
						<div class='pf-item one-col'>
							<div class='pf-label'>Name</div>
							<div class='pf-input'>
								<form:input type="text" path="name"/>
								<form:errors path="name"/>
							</div>
						</div>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Category <span class='help'>Separate multiple categories with commas.<br/>Prefix a category with ${tutorCategoryPrefix} if you want that category to only be visible to tutors.</span></div>
								<div class='pf-input'>
									<form:input type="text" path="category"/>
									<form:errors path="category"/>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Assessment marks</div>
								<div class='pf-input'>
									<form:input type="text" path="marks"/>
									<form:errors path="marks"/>
								</div>
							</div>
						</div>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Assessment due date</div>
								<div class='pf-input'>
									<form:input type="text" path="strDate" />
									<form:errors path="dueDate"/>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Late submission due date <span class='help'>Leave blank to not allow late submissions.</span></div>
								<div class='pf-input'>
									<form:input type="text" path="strLateDate" />
									<form:errors path="lateDate"/>
								</div>
							</div>
						</div>
					</div>
					<div class='pf-section'>
						<div class='pf-item'>
							<label>
								<input type="checkbox" id='toggleGroups' <c:if test="${assessment.groupWork}">checked='checked'</c:if> />
								Allow group work
							</label>
						</div>
						<div class='pf-horizontal two-col groupData'>
							<div class='pf-item'>
								<div class='pf-label'>Number of groups</div>
								<div class='pf-input'>
									<form:input type="number" min="0" path="groupCount" />
									<form:errors path="groupCount"/>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'></div>
								<div class='pf-input'>
									<label><input type="checkbox" id='unlimitedGroups' <c:if test="${assessment.groupCount == -1}">checked='checked'</c:if> /> No limit</label>
								</div>
							</div>
						</div>
						<div class='pf-horizontal two-col groupData'>
							<div class='pf-item'>
								<div class='pf-label'>Maximum group size</div>
								<div class='pf-input'>
									<form:input type="number" min="2" path="groupSize" />
									<form:errors path="groupSize"/>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'></div>
								<div class='pf-input'>
									<label><input type="checkbox" id='unlimitedSize' <c:if test="${assessment.groupSize == -1}">checked='checked'</c:if> /> No limit</label>
								</div>
							</div>
						</div>
						<div class='pf-horizontal two-col groupData'>
							<div class='pf-item'>
								<div class='pf-label'>Groups finalised <span class='help'>The date when students will no longer be able to move between groups. Tutors will still be able to assign groups after this date.</span></div>
								<div class='pf-input'>
									<form:input type="text" path="strGroupLock" />
									<form:errors path="groupLockDate"/>
								</div>
							</div>
						</div>
						<div class='pf-item groupData'>
							<form:checkbox path="studentsManageGroups"/>
							<label for="studentsManageGroups1">Allow students to manage group membership</label>
						</div>
					</div>
					<div class='pf-section'>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Maximum number of allowed submissions</div>
								<div class='pf-input'>
									<form:input type="text" path="numSubmissionsAllowed"/>
									<form:errors path="numSubmissionsAllowed"/>
								</div>
							</div>
						</div>
						<div class='pf-item'>
							<form:checkbox path="countUncompilable"/>
							<label for="countUncompilable1">Count submissions that have failed to compile</label>
						</div>
					</div>
					<div class='pf-section'>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Solution name <span class='help'>The name of the main solution source code file. <strong>Only required if you use Black Box unit tests.</strong> If students are to submit <code>MyProgram.java</code> and <code>MyProgram.c</code>, then solution name should be "MyProgram"</span></div>
								<div class='pf-input'>
									<form:input path="solutionName"/>
									<form:errors path="solutionName"/>
								</div>
							</div>
						</div>
						<div class='pf-item'>
							<div class='pf-label'>Allowed languages</div>
							<div class='pf-input'>
								<form:errors path="strLanguages" element="div"/>
								<form:select path="strLanguages" multiple="multiple" cssClass="langSelect">
									<form:options items="${allLanguages}" itemLabel="name" itemValue="id"/>
								</form:select>
							</div>
						</div>
					</div>
					<div class='pf-section'>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Upload custom validator <a href="../../help/customValidation/" target="_blank"><span class='help'>Click here for information about custom validators.</span></a></div>
								<div class='pf-input'>
									<form:input type="file" path="validatorFile"/> <form:errors path="validatorFile"/>
								</div>
							</div>
							<c:if test="${assessment.customValidator}">
								<div class='pf-item'>
									<div class='pf-label'>Current validator</div>
									<div class='pf-input'>
										<jsp:include page="../../recursive/fileWriterRoot.jsp">
											<jsp:param name="owner" value="assessment"/>
											<jsp:param name="fieldId" value="${assessment.id}"/>
										</jsp:include>
									</div>
								</div>
							</c:if>
						</div>
					</div>
					<div class='button-panel'>
						<button type="submit">Save Assessment</button>
					</div>
				</div>
			</div>
		</div>
		
		<div class='section'>
			<h2 class='section-title'>Release Rule</h2>
			<div class='part'>
				<tag:releaseRulePrinted rule="${assessment.releaseRule}" />
				<div class='button-panel'>
					<c:set var="buttonText" value="Set" />
					<c:if test="${assessment.released}"><c:set var="buttonText" value="Modify" /></c:if>
					<button type='button' class='flat' onclick='location.href="../release/${assessment.id}/"'><c:out value="${buttonText}" /> Release Rule</button>
				</div>
			</div>
		</div>
		
		<div class='section'>
			<h2 class='section-title'>Description</h2>
			<div id='descriptionHTML' class='part no-line show-math'>
				${assessment.description}
			</div>
			<div class='part no-line' style="display:none">
				<form:textarea path="description" cols="110" rows="10" />
			</div>
			<div class='button-panel'>
				<button class="flat" id="modifyDescription">Modify Description</button>
				<button type="submit" style='display:none;'>Save Assessment</button>
			</div>
		</div>
		
		<div class='section'>
			<h2 class='section-title'>Modules</h2>
			<div class='part'>
				<h3 class='part-title'>Unit Tests</h3>
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
									<input class='custom-check select-check' id="selectedUnitTests${index.index}.selected" type='checkbox' <c:if test="${module.id != null or module.weight != 0 or module.groupWork}">checked="checked"</c:if> />
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
			
			<div class='part'>
				<h3 class='part-title'>Hand Marking</h3>
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
									<input class='custom-check select-check' id="selectedHandMarking${index.index}.selected" type='checkbox' <c:if test="${module.id != null or module.weight != 0 or module.groupWork}">checked="checked"</c:if> />
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
			
			<div class='button-panel'>
				<button type="submit">Save Assessment</button>
			</div>
		</div>
		
	</form:form>
</div>

<script>
    $(function() {
    	$( "#strDate" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	$( "#strLateDate" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	$( "#strGroupLock" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	
    	$( ".moduleTable input:text" ).each(function() {
    		$textBox = $(this);
    		$textBox.tipsy({trigger: 'focus', gravity: 'n', title: function() {
    			if(!$(this).parents("tr").find("input[type='checkbox']").is(":checked")) {
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
    		$(".groupData").toggle(groupWork);
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
        	e.preventDefault();
        	$("#description").parents('div').show();
        	$("#modifyDescription").siblings().show();
        	$("#modifyDescription").hide();
        	
        	tinymce.init({
                selector: "#description",
                plugins: "table code link textcolor",
                toolbar: "undo redo | styleselect | forecolor backcolor | bold italic | alignleft aligncenter alignright alignjustify | code-styles-split | latex-split | bullist numlist outdent indent | link | code",
                setup: function(editor) {
                    editor.on('keyup', function() {
                    	updateDescription(600);
                    });
                    editor.on('change', function() {
                    	updateDescription();
                    });
                    editor.addButton('code-styles-split', {
                        type: 'splitbutton',
                        text: 'code',
                        title: 'Toggle <code> tags',
                        icon: false,
                        onclick: function() {
                        	editor.execCommand('mceToggleFormat', false, 'code');
                        },
                        menu: [
                            {text: 'Inline <code>', onclick: function() {
                            	editor.execCommand('mceToggleFormat', false, 'code');
                            }},
                            {text: 'Block <pre>', onclick: function() {
                            	editor.execCommand('mceToggleFormat', false, 'pre');
                            }}
                        ]
                    });
                    
                    function toggleEquation(sign) {
                    	var content = editor.selection.getContent({format: 'html'});
                    	if(!content) {
                        	editor.execCommand('mceInsertContent', false, sign + " " + sign);
                    	} else {
                    		editor.execCommand('mceReplaceContent', false, sign + content + sign);
                    	}
                    }
                    
                    editor.addButton('latex-split', {
                        type: 'splitbutton',
                        text: 'LaTeX',
                        title: 'Insert LaTeX equation',
                        icon: false,
                        onclick: function() { toggleEquation('$$'); },
                        menu: [
                            {
                            	text: 'Insert inline equation', 
                            	onclick: function() { toggleEquation('$$') }
                            }, {
                            	text: 'Insert block equation', 
                            	onclick: function() { toggleEquation('$$$') }
                            }
                        ]
                    });
                },
                style_formats_merge: true,
            });
        });
        
        var timer;
        function updateDescription(wait) {
        	clearTimeout(timer);
        	timer = setTimeout(function() {
        		$("#descriptionHTML").html(tinymce.activeEditor.getContent());
        		preview.refresh();
        	}, wait ? wait : 0);
        }

		// When you click on a selected checkbox, mark the row as selected
		$("input.select-check").on('click', function() {
			var $row = $(this).parents("tr");
			if ($(this).is(":checked")) {
				var $weightBox = $row.find("td input[type='text']");
				var weight = $weightBox.val();
				if (weight == 0) {
					$weightBox.val("1.0");
				}
			}
		});

		$("#updateAssessmentForm").on(
				"submit",
				function() {
					//Only visible rows will be properly selected, so make them all visible
					$("table.moduleTable").each(function() {
						$(this).DataTable().page.len(-1).search("").draw();
					});

					// Ignore non-selected modules
					$("input.select-check:not(:checked)")
						.parents("tr")
						.hide()
						.find("input")
						.prop("disabled", true)

					// Save the description
					if(tinymce.activeEditor) {
						$("#description").val(tinymce.activeEditor.getContent());
					}
				});

		var i = 0;
		// Initialise module tables
		$.fn.dataTable.ext.order['dom-checkbox'] = function(settings, col) {
			return this.api().column(col, {
				order : 'index'
			}).nodes().map(function(td, i) {
				return $('input[type="checkbox"]', td).is(':checked') ? '0' : '1';
			});
		};
		$.fn.dataTable.ext.order['dom-text'] = function(settings, col) {
			return this.api().column(col, {
				order : 'index'
			}).nodes().map(function(td, i) {
				return $('input[type="text"]', td).val() * 1;
			});
		};
		$("table.moduleTable").each(function() {
			var cols = $(this).find("tr:first th").length;
			$(this).addClass("display");
			$(this).attr("width", "100%");
			$(this).DataTable({
				deferDrawing : true,
				paging : true,
				info : true,
				searching : true,
				ordering : true,
				order: [[0, "asc"], [1, "asc"]],
				columnDefs : [ {
					targets : cols < 5 ? [0, 3] : [0, 3, 4],
					orderDataType : "dom-checkbox"
				}, {
					targets: [2],
					orderDataType : "dom-text"
				} ],
				language : {
					emptyTable : "None available."
				}
			});
		});

		$("#hideUntilLoaded").show();
		$("#loading").hide();
	});
</script>



<script>
// For updating all MathJax on page, call preview.refresh()
var preview = {
  mjRunning: false,  // true when MathJax is processing

  update: function () {
    if (this.mjRunning) return;
    this.mjRunning = true;
    MathJax.Hub.Queue(
      ["Typeset",MathJax.Hub],
      ["previewDone",this]
    );
  },

  previewDone: function () {
    this.mjRunning = false;
  }
};

preview.refresh = MathJax.Callback(["update",preview]);
preview.refresh.autoReset = true;  // make sure it can run more than once
</script>
