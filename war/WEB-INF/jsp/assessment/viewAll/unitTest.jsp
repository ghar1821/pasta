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
