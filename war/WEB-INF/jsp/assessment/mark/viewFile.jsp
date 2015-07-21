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
<%@ taglib prefix="pasta" uri="pastaTag"%>

<c:set var="dlc" value="${fn:replace(location, '\\\\', '\\\\\\\\')}"/>
<h1>${owner}</h1>
<h4><code>${filename}</code></h4>
<c:choose>
	<c:when test="${fileEnding == 'jpg' || fileEnding == 'png' || fileEnding == 'bmp' || fileEnding == 'gif'}">
		<img src='loadFile?file_name="${location}"'/>
	</c:when>
	<c:when test="${fileEnding == 'pdf'}">
		<object data='loadFile?file_name="${location}"' type="application/pdf" width="90%" height="500"></object>
	</c:when>
	<c:when test="${not empty codeStyle[fileEnding]}">
		<pre class="${codeStyle[fileEnding]}"><code><c:out value='${fileContents}'/></code></pre>
	</c:when>
	<c:when test="${pasta:isPlainText(location)}">
		<div class='boxCard'>
			<pre><code><c:out value='${fileContents}'/></code></pre>
		</div>
	</c:when>
	<c:otherwise>
		<c:redirect url='../downloadFile?file_name="${location}"'/>
	</c:otherwise>
</c:choose>
<button onclick='location.href="../downloadFile?file_name=\"${dlc}\"";'>Download file</a>