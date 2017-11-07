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

<h1>Reporting<c:if test="${not empty pretending}"> - ${pretending.username}</c:if></h1>

<c:if test="${not empty pretending}">
	<div class='section'>
		<div class='part'>
			<span class='warning'><span class='fa fa-warning'></span> Warning:</span>
			You are viewing reports as if you were <a href='<c:url value="/student/${pretending.username}/home/"/>'>${pretending.username}</a>.
			Click <a href='<c:url value="/reporting/"/>'>here</a> to view your own reports.
		</div>
	</div>
</c:if>

<c:if test="${empty allReports}">
	<div class='section'>
		<div class='part'>
			<span>No reports to display at this time.</span>
		</div>
	</div>
</c:if>
<c:forEach var="report" items="${allReports}">
	<div class='report'>
		<div class='section-title'>
			<h3 class='report-name'>${report.name}</h3>
			<p>${report.description}</p>
		</div>
		<c:if test="${user.instructor and (empty pretending or pretending.instructor) }">
			<div class='report-controls part'>
				<span class='fa fa-eye'></span>
				<span><a class='edit-permissions'>Who can see this report?</a></span>
			</div>
		</c:if>
		<div class='report-content' data-report='${report.id}'>
		</div>
	</div>
</c:forEach>

<script src='<c:url value="/static/scripts/reporting/reporting.js"/>'></script>
<c:forEach var="report" items="${allReports}">
<script src='<c:url value="/static/scripts/reporting/reports/"/>${report.id}.js'></script>
</c:forEach>