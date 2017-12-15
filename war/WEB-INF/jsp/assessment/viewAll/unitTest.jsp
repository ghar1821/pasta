<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class='float-container'>
	<div class='horizontal-block'>
		<h1>Unit Tests</h1>
		<h4>Click <a href='../help/unitTests/'>here</a> for help with unit tests.</h4>
	</div>
	<input id='search' type='text' />
</div>

<div class='section'>
	<c:forEach var="unitTest" items="${allUnitTests}">
		<div class='unitTest part row'>
			<div>
				<span class='testName larger-text'>${unitTest.name}</span> -
				<span class="pastaTF pastaTF${unitTest.tested}">
					<c:choose>
						<c:when test="${unitTest.tested}">
							Tested
						</c:when>
						<c:otherwise>
							Untested
						</c:otherwise>
					</c:choose>
				</span>
			</div>
			<div class='button-panel'>
				<button class='flat hbn-button' data-hbn-icon="fa-info" onclick='location.href="./${unitTest.id}/"'>Details</button>
				<c:if test="${user.instructor}">
					<button class='flat hbn-button hbn-confirm' data-hbn-icon="fa-trash" onclick="location.href='./delete/${unitTest.id}/'">Delete</button>
				</c:if>
			</div>
		</div>
	</c:forEach>
</div>

<c:if test="${user.instructor}">
	<button id="newPopup" class='floating plus'></button>
</c:if>

<div id="newUnitTestDiv" class='popup' >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<div class='part'>
		<h1 class='part-title'> New Unit Test </h1>
		<form:form commandName="newUnitTest" enctype="multipart/form-data" method="POST">
			<div class='pasta-form narrow'>
				<div class='pf-item one-col'>
					<div class='pf-label'>Name</div>
					<div class='pf-input'>
						<form:errors path="name" element="div" />
						<form:input autocomplete="off" type="text" path="name" />
					</div>
				</div>
				<div class='button-panel'>
					<button type="submit" id="submit">Create</button>
				</div>
			</div>
		</form:form>
	</div>
</div>

	
<script>
	$(function() {
	    $('#newPopup').on('click', function(e) {
	        e.preventDefault();
	        $('#newUnitTestDiv').bPopup();
	    });
	});
	<spring:hasBindErrors name='newUnitTest'>
		$('#newUnitTestDiv').bPopup();
	</spring:hasBindErrors>
	
	$(".unitTest").searchNode();
	$(".unitTest").find(".testName").searchable();
	var searchBox = $("#search").searchBox();
	
	$(".hbn-button").hoverButton({
		dataKey: "hbn-icon"
	});
</script>
