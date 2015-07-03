<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<ul class="jqueryFileTree">
	<c:set var="node" value="${node}" scope="request"/>
	<jsp:include page="fileWriter.jsp"/>
</ul>
<form:form id='viewFileForm' action="${pageContext.request.contextPath}/viewFile/" method="post" target="_blank">
	<input type="hidden" name="location" value=""/>
</form:form>
<script>
$(function() {
	$(document.body).append($("<form id='viewFileForm' action='${pageContext.request.contextPath}/viewFile/' method='post' target='_blank'><input type='hidden' name='location' value=''/></form>"));
	$("li.leaf").on("click", function() {
		$('#viewFileForm').find("input").val($(this).attr('location'));
		$('#viewFileForm').submit();
	});
});
</script>
