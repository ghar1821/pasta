<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<ul class="jqueryFileTree">
	<c:set var="node" value="${node}" scope="request"/>
	<jsp:include page="fileWriter.jsp"/>
</ul>
<script>
$(function() {
	if(!$("#viewFileForm").length) {
		var $form = $("<form />", {
			id : "viewFileForm",
			action : "${pageContext.request.contextPath}/viewFile/",
			method : "post",
			target : "_blank"
		});
		$form.append($("<input />", {
			type : "hidden",
			id : "location",
			name : "location",
			value : ""
		}));
		$form.append($("<input />", {
			type : "hidden",
			id : "owner",
			name : "owner",
			value : ""
		}));
		$form.append($("<input />", {
			type : "hidden",
			id : "fieldId",
			name : "fieldId",
			value : ""
		}));
		$(document.body).append($form);
	}
	
	$("li.leaf").on("click", function() {
		$('#viewFileForm').find("input#location").val($(this).attr('location'));
		$('#viewFileForm').find("input#owner").val($(this).attr('owner'));
		$('#viewFileForm').find("input#fieldId").val($(this).attr('fieldId'));
		$('#viewFileForm').submit();
	});
});
</script>
