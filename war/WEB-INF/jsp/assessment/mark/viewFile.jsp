<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
	<c:when test="${not empty codeStyle[fileEnding]}">
		<pre class="${codeStyle[fileEnding]}"><code>${fileContents}</code></pre>
	</c:when>
	<c:otherwise>
		<pre>${fileContents}</pre>
	</c:otherwise>
</c:choose>
