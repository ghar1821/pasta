<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if test="${not empty validationResults.errors or not empty validationResults.feedback}">
	<div>
		<h3>Quick Feedback - ${validationResults.assessmentName}</h3>
		<div class='vrf-feedback'>
			<c:if test="${not empty validationResults.errors}">
				<div class='vrf-parent vertical-box padded ui-state-error ui-corner-all'>
					<div part='error' class='vrf-del float-right icon_delete'></div>
					<c:forEach var="errorList" items="${validationResults.errorsMap}">
						<c:if test="${not empty errorList.key}">
							<p><strong><c:out value="${errorList.key}" /></strong>
						</c:if>
						<ul>
							<c:forEach var="error" items="${errorList.value}">
								<li><c:if test="${error.preFormat}"><pre></c:if><c:out value="${error.feedback}" /><c:if test="${error.preFormat}"></pre></c:if>
							</c:forEach>
						</ul>
					</c:forEach>
				</div>
			</c:if>
			<c:if test="${not empty validationResults.feedback}">
				<div class='vrf-parent vertical-box padded ui-state-highlight ui-corner-all'>
					<div part='feedback' class='vrf-del float-right icon_delete'></div>
					<c:forEach var="feedbackList" items="${validationResults.feedbackMap}">
						<c:if test="${not empty feedbackList.key}">
							<p><strong><c:out value="${feedbackList.key}" /></strong>
						</c:if>
						<ul>
							<c:forEach var="feedback" items="${feedbackList.value}">
								<li><c:if test="${feedback.preFormat}"><pre></c:if><c:out value="${feedback.feedback}" /><c:if test="${feedback.preFormat}"></pre></c:if>
							</c:forEach>
						</ul>
					</c:forEach>
				</div>
			</c:if>
		</div>
	</div>
</c:if>
<script>
$(".vrf-del").on("click", function() {
	$.ajax({
		url : "clearValidationResults/",
		data : {"part" : $(this).attr("part")},
		dataType: "text",
		type : "POST",
		success : function(data) {
			console.log("DATA: " + data);
		}
	});
	$(this).closest(".vrf-parent").slideToggle("fast", function() {
		$(this).remove();
		if($(".vrf-feedback").children().length == 0) {
			$(".vrf-feedback").parent().slideToggle("fast", function() {
				$(this).remove();
			});
		}
	});
});
</script>