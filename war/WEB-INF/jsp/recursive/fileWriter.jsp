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

<c:choose>
	<c:when test="${not node.leaf}">
		<li class="list">
			<div style="cursor:pointer" onclick="$(this).children().toggleClass('ui-icon-folder-collapsed ui-icon-folder-open');$(this).parent().next().toggle('fast');">
				<span style="float:left;" class="ui-icon ui-icon-folder-collapsed">poo</span>
				${node.name}
			</div>
		</li>
		<ul class="list" style="display:none">
			<c:forEach var="node" items="${node.children}">
				<c:set var="node" value="${node}" scope="request"/>
				<jsp:include page="fileWriter.jsp"/>
			</c:forEach>
		</ul>
	</c:when>
	<c:otherwise>
		<li class="list">
			<form action="${pageContext.request.contextPath}/viewFile/" method="post" target="_blank">
				<input type="hidden" name="location" value="${node.location}"/>
			    <div style="cursor:pointer" onclick="this.parentNode.submit()">
			    	<span style="float:left;" class="ui-icon ui-icon-document"></span>
			    	${node.name}
			    </div>
			</form>
		</li>
	</c:otherwise>
</c:choose>
