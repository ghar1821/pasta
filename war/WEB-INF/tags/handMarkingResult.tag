<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<%@ attribute name="user" required="true" type="pasta.domain.user.PASTAUser" rtexprvalue="true"%>
<%@ attribute name="results" required="true" type="pasta.domain.result.AssessmentResult" rtexprvalue="true"%>
<%@ attribute name="marking" required="true" type="Boolean"%>

<%@ attribute name="heading" required="false" type="String"%>
<%@ attribute name="headingLevel" required="false" type="String"%>

<c:if test="${empty importedDisplay}">
	<script src='<c:url value="/static/scripts/assessment/displayHandMarkingResults.js"/>'></script>
	<c:set var="importedDisplay" value="true" scope="request" />
</c:if>
<c:if test="${marking and empty importedMarking}">
	<script src='<c:url value="/static/scripts/assessment/markHandMarkingResults.js"/>'></script>
	<c:set var="importedMarking" value="true" scope="request" />
</c:if>

<c:set var="relevantWeightedHandMarking" value="${results.groupResult ? results.assessment.groupHandMarking : results.assessment.individualHandMarking}" />
<c:set var="handMarkingResultList" value="${results.handMarkingResults}" />

<c:if test="${marking or (not empty relevantWeightedHandMarking and results.finishedHandMarking)}">
	<<c:out value="${headingLevel}" default="h3"/> class='compact'><c:out value="${heading}" default="Hand Marking"/></<c:out value="${headingLevel}" default="h3"/>>
	<div class='all_hand_marking_wrapper vertical-block'>
		<c:if test="${empty handMarkingResultList}">
			<p>No hand marking for <c:out value="${results.groupResult ? 'group' : 'individual'}" /> work submissions.
		</c:if>
		<c:forEach var="handMarkingResult" items="${handMarkingResultList}" varStatus="hmResultStatus">
			<c:set var="weightedHM" value="${handMarkingResult.weightedHandMarking}" />
			<c:set var="handMarking" value="${weightedHM.handMarking}" />
			<div class='hand_marking_wrapper vertical-block'>
				<c:if test="${marking}">
					<h4 class='compact'>${handMarking.name}</h4>
					<input type='hidden' name="handMarkingResults[${hmResultStatus.index}].id" value="${handMarkingResult.id}" />
					<input type="hidden" name="handMarkingResults[${hmResultStatus.index}].weightedHandMarking.id" value="${weightedHM.id}" />
				</c:if>
				
				<table class="hm-table" style="table-layout:fixed; overflow:auto; width:100%">
					<thead>
						<tr>
							<th></th> <%-- empty on purpose --%>
							<c:forEach var="column" items="${handMarking.columnHeader}" varStatus="columnStatus">
								<th class='hm-col-header ${marking?"marking":""}'  <%-- onclick="clickAllInColumn(this.cellIndex, ${hmResultStatus.index})" --%>>
									${column.name}<br />
									<fmt:formatNumber type="percent" maxFractionDigits="1" value="${column.weight}" /><br />
								</th>
							</c:forEach>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${handMarking.rowHeader}">
							<tr class='hm-row'>
								<th class='hm-row-header'>
									${row.name}<br />
									<fmt:formatNumber type="number" maxIntegerDigits="3" value="${weightedHM.maxMark * row.weight}"/>
								</th>
								<c:forEach var="column" items="${handMarking.columnHeader}">
									<td id="cell_${handMarkingResult.id}_${row.id}_${column.id}"
									 class="hm-cell empty ${marking?'marking':''} <c:if test="${handMarkingResult.result[row.id] == column.id}">selectedMark</c:if>"><%--Filled with JS--%></td>
								</c:forEach>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				
				<c:if test="${marking}">
					<input id='submit' class="save_hand_marking" type="submit" value="Save"/>
				</c:if>
			</div>
		</c:forEach>
	</div>
	<script>
	fillArray.push(function() {
		var $cell;
		<c:forEach var="handMarkingResult" items="${handMarkingResultList}" varStatus="hmResultStatus">
			<c:set var="weightedHM" value="${handMarkingResult.weightedHandMarking}" />
			<c:set var="handMarking" value="${weightedHM.handMarking}" />
			<c:forEach var="datum" items="${handMarking.data}">
				$cell = $("#cell_${handMarkingResult.id}_${datum.row.id}_${datum.column.id}");
				<c:if test="${datum.data != null}">
					${marking ? 'fillMarkingCell' : 'fillCell'}($cell,
						<fmt:formatNumber type='number' maxIntegerDigits='3' value='${weightedHM.maxMark * datum.row.weight * datum.column.weight}' />,
						"${pasta:escapeNewLines(datum.data)}"<c:if test="${marking}">,
						${handMarkingResult.result[datum.row.id] == datum.column.id},
						${hmResultStatus.index},
						${datum.column.id},
						${datum.row.id}
						</c:if>
					);
				</c:if>
			</c:forEach>
		</c:forEach>
	});
	</script>
</c:if>