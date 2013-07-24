<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="dlc" value="${fn:replace(location, '\\\\', '\\\\\\\\')}"/>
<c:choose>
	
	<c:when test="${fileEnding == 'jpg' || fileEnding == 'png' || fileEnding == 'bmp' || fileEnding == 'gif'}">
		<img src='loadFile?file_name="${location}"'/>

	</c:when>
		<c:when test="${fileEnding == 'pdf'}">

		<object data='loadFile?file_name="${location}"' type="application/pdf" width="90%" height="500">
   </object>
	</c:when>
	<c:when test="${not empty codeStyle[fileEnding]}">
		<pre class="${codeStyle[fileEnding]}"><code>${fileContents}</code></pre>
	</c:when>
	<c:otherwise>
		<c:redirect url='../downloadFile?file_name="${location}"'/>
	</c:otherwise>
</c:choose>
<button onclick='location.href="../downloadFile?file_name=\"${dlc}\"";'>Download file</a>