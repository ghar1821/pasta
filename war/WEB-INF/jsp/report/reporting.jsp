<!-- 
Copyright (c) 2017, Joshua Stretton
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
<%@ taglib prefix="pasta" uri="pastaTag"%>

<h1>Reporting</h1>


<c:forEach var="report" items="${allReports}">
	<div class='report section'>
		<h3 class='section-title report-name'>${report.name}</h3>
		<c:if test="${user.instructor}">
			<div class='report-controls part'>
				<span class='fa fa-eye'></span>
				<c:choose>
					<c:when test="${empty report.permissionLevels}"><a class='edit-permissions'><span class='permission'>Nobody</span></a></c:when>
					<c:otherwise>
						<a class='edit-permissions'><c:forEach var="permission" items="${report.permissionLevels}"><%--
							--%><span class='permission'>${permission.description}</span><%--
						--%></c:forEach></a>
					</c:otherwise>
				</c:choose>
			</div>
		</c:if>
		<div class='report-content part' data-report='${report.id}'>
			<button class='load-report'>Load</button>
		</div>
	</div>
</c:forEach>

<script>
	var allPermissions = [];
	<c:forEach var="permission" items="${allPermissions}">
		allPermissions.push({
			text: "${permission.description}",
			value: "${permission}"
		});
	</c:forEach>
</script>

<script src='<c:url value="/static/scripts/reporting/reporting.js"/>'></script>
<c:forEach var="report" items="${allReports}">
<script src='<c:url value="/static/scripts/reporting/reports/"/>${report.id}.js'></script>
</c:forEach>