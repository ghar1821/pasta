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