<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1 style="margin-bottom:0.5em;">${assessment.name}</h1>

<c:if test="${not assessment.completelyTested}" >
	<div class="ui-state-error ui-corner-all" style="font-size: 1.5em;">
		<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span><b>WARNING: This assessment contains untested unit tests</b>
	</div>
</c:if>

<button style="margin-top:1em;float:left;" class="button" onClick="window.location.href=window.location.href+'run/'">Re-Run Assessment</button>

<form:form commandName="assessment" enctype="multipart/form-data" method="POST">
	
	<input type="submit" value="Save Assessment" id="submit" style="margin-top:1em;"/>
	
	<table>
		<tr><td>Assessment Marks:</td><td><form:input type="text" path="marks"/></td></tr>
		<tr><td>Assessment DueDate:</td><td><form:input type="text" path="simpleDueDate" id="simpleDueDate" name="simpleDueDate"/></td></tr>
		<tr><td>Maximum Number of allowed submissions:</td><td><form:input type="text" path="numSubmissionsAllowed"/></td></tr>
	</table>
	
	The assessment has <c:if test="${not assessment.released}"> not </c:if> been released
	
	<h2>Description</h2>
	<div id="descriptionHTML">
		${assessment.description}
	</div>
	<button type="button" id="modifyDescription">Modify Description</button>
	<form:textarea path="description" cols="110" rows="10" style="display:none"/>
	
	<table style="margin-bottom:2em;width:90%">
		<tr>
			<td valign="top" >
				<div style="float:left">
					<h2> Unit Tests </h2>
					<table>
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th><th>Tested</th></tr>
						<tbody id="unitTest" class="sortable unitTests">
							<c:forEach var="unitTest" varStatus="unitTestIndex" items="${assessment.unitTests}">
								<tr>
									<td>
										<form:input type="hidden" path="unitTests[${unitTestIndex.index}].unitTestName" value="${unitTest.unitTestName}"/>
										<a href="../../unitTest/${unitTest.test.shortName}/">${unitTest.test.name}</a>
									</td>
									<td><form:input size="5" type="text" path="unitTests[${unitTestIndex.index}].weight" value="${unitTest.weight}"/></td>
									<td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				
					<h2> Secret Unit Tests </h2>
					<table>
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th><th>Tested</th></tr>
						<tbody id="unitTest" class="sortable secretUnitTests">
							<c:forEach var="unitTest" varStatus="unitTestIndex" items="${assessment.secretUnitTests}">
								<tr>
									<td>
										<form:input type="hidden" path="secretUnitTests[${unitTestIndex.index}].unitTestName" value="${unitTest.unitTestName}"/>
										<a href="../../unitTest/${unitTest.test.shortName}/">${unitTest.test.name}</a>
									</td>
									<td><form:input size="5" type="text" path="secretUnitTests[${unitTestIndex.index}].weight" value="${unitTest.weight}"/></td>
									<td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</td>
			<td valign="top">
				<div style="float:left">
					<h2> Available Unit Tests </h2>
					<table>
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th><th>Tested</th></tr>
						<tbody id="unitTest" class="sortable garbage">
							<c:forEach var="unitTest" varStatus="unitTestIndex" items="${otherUnitTests}">
								<tr>
									<td>
										<form:input type="hidden" path="garbage[${unitTestIndex.index}].unitTestName" value="${unitTest.unitTestName}"/>
										<a href="../../unitTest/${unitTest.test.shortName}/">${unitTest.test.name}</a>
									</td>
									<td><form:input size="5" type="text" path="garbage[${unitTestIndex.index}].weight" value="${unitTest.weight}"/></td>
									<td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
	</table>
	
	<table style="margin-bottom:2em;width:90%">
		<tr>
			<td valign="top" >
				<div style="float:left">
					<h2> Hand Marking </h2>
					<table>
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th></tr>
						<tbody id="handMarking" class="sortable handMarking">
							<c:forEach var="handMarking" varStatus="handMarkingIndex" items="${assessment.handMarking}">
								<tr>
									<td>
										<form:input type="hidden" path="handMarking[${handMarkingIndex.index}].handMarkingName" value="${handMarking.handMarkingName}"/>
										<a href="../../handMarking/${handMarking.handMarking.shortName}/">${handMarking.handMarking.name}</a>
									</td>
									<td><form:input size="5" type="text" path="handMarking[${handMarkingIndex.index}].weight" value="${handMarking.weight}"/></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</td>
			<td valign="top">
				<div style="float:left">
					<h2> Available Hand Marking</h2>
					<table>
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th></tr>
						<tbody id="handMarking" class="sortable handGarbage">
							<c:forEach var="handMarking" varStatus="handMarkingIndex" items="${otherHandMarking}">
								<tr>
									<td>
										<form:input type="hidden" path="handGarbage[${handMarkingIndex.index}].handMarkingName" value="${handMarking.handMarkingName}"/>
										<a href="../../handMarking/${handMarking.handMarking.shortName}/">${handMarking.handMarking.name}</a>
									</td>
									<td><form:input size="5" type="text" path="handGarbage[${handMarkingIndex.index}].weight" value="${handMarking.weight}"/></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
	</table>
	
	<input type="submit" value="Save Assessment" id="submit"/>
</form:form>

<script>
    $(function() {
    	$( "#simpleDueDate" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	
        $( "#unitTest.sortable" ).sortable({
            connectWith: "tbody",
            dropOnEmpty: true,
            
            stop: function(event, ui){
            	var tables = $("#unitTest.sortable");
            	// for each table
            	for(var i=0; i<tables.length; i++){
            		// for each child
            		var prefix = $.trim(tables[i].className.replace("sortable", "").replace("ui-sortable", ""));
            		var childrenNodes = tables[i].children;
            		for(var j=0; j<childrenNodes.length; ++j){
            			
            			// td -> input - unitTestName
            			childrenNodes[j].children[0].children[0].setAttribute("id", prefix+j+".unitTestName");
            			childrenNodes[j].children[0].children[0].setAttribute("name", prefix+"["+j+"]"+".unitTestName");
            			
            			// td -> input - weight
            			childrenNodes[j].children[1].children[0].setAttribute("id", prefix+j+".weight");
            			childrenNodes[j].children[1].children[0].setAttribute("name", prefix+"["+j+"]"+".weight");
            		}
            	}
            }
        });
        
        $( "#handMarking.sortable" ).sortable({
            connectWith: "tbody",
            dropOnEmpty: true,
            
            stop: function(event, ui){
            	var tables = $("#handMarking.sortable");
            	// for each table
            	for(var i=0; i<tables.length; i++){
            		// for each child
            		var prefix = $.trim(tables[i].className.replace("sortable", "").replace("ui-sortable", ""));
            		var childrenNodes = tables[i].children;
            		for(var j=0; j<childrenNodes.length; ++j){
            			
            			// td -> input - unitTestName
            			childrenNodes[j].children[0].children[0].setAttribute("id", prefix+j+".handMarkingName");
            			childrenNodes[j].children[0].children[0].setAttribute("name", prefix+"["+j+"]"+".handMarkingName");
            			
            			// td -> input - weight
            			childrenNodes[j].children[1].children[0].setAttribute("id", prefix+j+".weight");
            			childrenNodes[j].children[1].children[0].setAttribute("name", prefix+"["+j+"]"+".weight");
            		}
            	}
            }
        });
        
        $( "tbody.sortable").disableSelection();
        
        $("#modifyDescription").bind('click', function(e) {
        	$("#description").show();
        	$("#modifyDescription").hide();
        });
        
        $("#description").on('keyup', function() {
            $("#descriptionHTML").html(document.getElementById("description").value);
        });
    });
</script>

<!-- TODO add for hand marking and competitions # 46-->