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
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>

<c:set var="owner" value="${assessmentName} - ${student.username}" />
<c:if test="${student.group}">
	<c:set var="owner" value="${student.name}" />
</c:if>

<h1>${owner}</h1>

<div class='vertical-block'>
	<jsp:include page="../../recursive/fileWriterRoot.jsp">
		<jsp:param name="owner" value="${owner}"/>
	</jsp:include>
</div>
<div class='vertical-block'>
	<form:form commandName="assessmentResult" enctype="multipart/form-data" method="POST">
	
		<div class='vertical-block boxCard'>
			<h3 class='compact'>Automatic Marking Results</h3>
			<div class='vertical-block'>
				<h4 class='compact'>Summary</h4>
				<tag:unitTestResult closedAssessment="false" user="${unikey}" results="${assessmentResult}" summary="true" />
			</div>
					
			<div id="${assessmentResult.id}" class='resultDetails vertical-block'>
				<h4 class='compact'><a id='detailsToggle'>Show Details</a></h4>
				<tag:unitTestResult closedAssessment="false" 
					user="${unikey}" results="${assessmentResult}" />
			</div>
		</div>
		
		<div class='vertical-block boxCard'>
			<tag:handMarkingResult user="${unikey}" results="${assessmentResult}" marking="true" heading="Hand Marking Guidelines" />
		</div>
	
		<div class='vertical-block boxCard'>
			<form:textarea style="height:200px; width:100%" path="comments"/>
		</div>
		
		<input type="submit" value="Save changes" id="submit" style="margin-top:1em;"/>
		
	</form:form>
</div>


<script src='<c:url value="/static/scripts/assessment/markHandMarking.js"/>'></script>