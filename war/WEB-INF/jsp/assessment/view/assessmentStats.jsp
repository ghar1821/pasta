<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1 style="margin-bottom:0.5em;">${assessment.name}</h1>
<h3>Mark distribution</h3>
<div id="markDistribution" style="height:300px; width:90%;"></div>
<h3>Number of submissions distribution</h3>
<div id="submissionDistribution" style="height:300px; width:90%;"></div>


<script>
$(document).ready(function(){
	
	var plot1 = $.jqplot ('markDistribution', [[
   		<c:forEach var="mark" items="${markDistribution}" varStatus="markStatus">
   			<c:if test="${not (markStatus.index == 0)}">
   				,
   			</c:if>
   			[${markStatus.index/maxBreaks*assessment.marks}, ${mark}]
   		</c:forEach>]], {
   		axesDefaults: {
   			pad: 0
   		}});
	
	
	var plot2 = $.jqplot ('submissionDistribution', [[
		<c:forEach var="dist" items="${submissionDistribution}" varStatus="distStatus">
			<c:if test="${not (distStatus.index == 0)}">
				,
			</c:if>
			[${dist.key}, ${dist.value}]
		</c:forEach>]], {
		axesDefaults: {
			pad: 0
		}});
});
</script>