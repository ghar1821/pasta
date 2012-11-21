<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form:form commandName="assessment" enctype="multipart/form-data" method="POST">
	
	<h1 style="margin-bottom:0.5em;">${assessment.name}</h1>
	
	<c:if test="${not assessment.completelyTested}" >
		<div class="ui-state-error ui-corner-all" style="font-size: 1.5em;">
			<span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span><b>WARNING: This assessment contains untested unit tests</b>
		</div>
	</c:if>
	
	<input type="submit" value="Update Assessment" id="submit" style="margin-top:1em;"/>
	
	<!--TODO make the values changable #45 -->
	<table>
		<tr><td>Assessment Marks:</td><td><form:input type="text" path="marks"/></td></tr>
		<tr><td>Assessment DueDate:</td><td><form:input type="text" path="simpleDueDate" id="simpleDueDate" name="simpleDueDate"/></td></tr>
		<tr><td>Maximum Number of allowed submissions:</td><td><form:input type="text" path="numSubmissionsAllowed"/></td></tr>
	</table>
	
	The assessment has <c:if test="${not assessment.released}"> not </c:if> been released
	
	<h2>Description</h2>
	<form:textarea path="description" cols="110" rows="10"/>
	
	<table style="margin-bottom:2em;">
		<tr>
			<td valign="top">
				<div style="float:left">
					<h2> Unit Tests </h2>
					<table>
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th><th>Tested</th></tr>
						<tbody class="sortable">
							<c:forEach var="unitTest" items="${assessment.unitTests}">
								<tr><td><a href="../../../unitTest/view/${unitTest.test.shortName}/">${unitTest.test.name}</a></td><td>${unitTest.weight}</td><td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td></tr>
							</c:forEach>
						</tbody>
					</table>
				
					<h2> Secret Unit Tests </h2>
					<table class="secretUnitTests">
						<tr class="sortableDisabled"><th>Name</th><th>Weighting</th><th>Tested</th></tr>
						<tbody class="sortable">
							<c:forEach var="unitTest" items="${assessment.secretUnitTests}">
								<tr><td><a href="../../../unitTest/view/${unitTest.test.shortName}/">${unitTest.test.name}</a></td><td>${unitTest.weight}</td><td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td></tr>
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
						<tbody class="sortable">
							<c:forEach var="unitTest" items="${otherUnitTests}">
								<tr><td><a href="../../../unitTest/view/${unitTest.test.shortName}/">${unitTest.test.name}</a></td><td>${unitTest.weight}</td><td class="pastaTF pastaTF${unitTest.test.tested}">${unitTest.test.tested}</td></tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
	</table>
	
	<input type="submit" value="Update Assessment" id="submit"/>
</form:form>

<script>
    $(function() {
    	$( "#simpleDueDate" ).datetimepicker({timeformat: 'hh:mm', dateFormat: 'dd/mm/yy'});
    	
        $( "tbody.sortable" ).sortable({
            connectWith: "tbody",
            dropOnEmpty: true
        });
        
        $( "tbody.sortable").disableSelection();
    });
</script>

<!-- TODO add for hand marking and competitions # 46-->