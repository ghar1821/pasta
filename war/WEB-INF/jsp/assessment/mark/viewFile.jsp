<%--
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
--%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pasta" uri="pastaTag"%>

<c:set var="dlc" value="${fn:replace(location, '\\\\', '\\\\\\\\')}"/>
<h1>${owner}</h1>
<div class='section part'>
	<h3><code>${filename}</code></h3>
	<c:choose>
		<c:when test="${fileEnding == 'jpg' || fileEnding == 'png' || fileEnding == 'bmp' || fileEnding == 'gif'}">
			<img src='loadFile?owner=${owner}&file_name="${location}"'/>
		</c:when>
		<c:when test="${fileEnding == 'pdf'}">
			<object data='loadFile?owner=${owner}&file_name="${location}"' type="application/pdf" class='pdf-reader'></object>
		</c:when>
		<c:when test="${not empty fileContents}">
			<pre><code class='${fileType}'><c:out value='${fileContents}'/></code></pre>
		</c:when>
		<c:otherwise>
			<c:redirect url='../downloadFile?owner=${owner}&file_name="${location}"'/>
		</c:otherwise>
	</c:choose>
	<div class='button-panel'>
		<button onclick='location.href="../downloadFile?owner=${owner}&file_name=\"${dlc}\"";'>Download file</button>
	</div>
</div>