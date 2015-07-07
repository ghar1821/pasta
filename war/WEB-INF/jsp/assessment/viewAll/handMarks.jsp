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

<h1>Hand Marking Templates</h1>

<table class="pastaTable">
	<c:forEach var="handMarking" items="${allHandMarking}">
		<tr>
			<td><b>${handMarking.name}</b></td>
			<td>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="location.href='./${handMarking.id}/'">Details</button>
				</div>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
					<button style="float:left; display:none; text-align: center; " onclick="location.href='./delete/${handMarking.id}/'" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
				</div>
			</td>
		</tr>
	</c:forEach>
</table>

<button id="newPopup">Add a new Hand marking template</button>

<div id="confirmPopup" class='popup'>
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<button id="confirmDeleteButton" onclick="">Confirm Deletion</button>
</div>

<div id="newHandMarking" class='popup' >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> New Hand Marking </h1>
	<form:form commandName="newHandMarkingModel" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Hand Marking template name:</td><td><form:input path="name"/> <form:errors path="name"/></td></tr>
		</table>
    	<input type="submit" value="Create" id="submit"/>
	</form:form>
</div>

	
<script>
	$(function() {
	    $('#newPopup').on('click', function(e) {
	        e.preventDefault();
	        $('#newHandMarking').bPopup();
	    });
	    <spring:hasBindErrors name='newHandMarkingModel'>
	    	$('#newHandMarking').bPopup();
		</spring:hasBindErrors>
	});
</script>