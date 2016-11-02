<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h1>Error</h1>

<div class='section'>
	<div class='part'>
		<p>There has been an error processing your request.
		<p>If this error persists, please contact an administrator with the details of what you were doing to create the error.
		<p><a href='<c:url value="/home/"/>'>Go Home</a> | <a href="javascript:window.history.back();">Go Back</a>
	</div>
</div>

<c:if test="${not empty user and user.tutor}">
	<div class='section'>
		<h2 class='section-title'>Error Details</h2>
		<div class='part'>
			<dl>
				<dt>User</dt>
				<dd><p>${user.username}</dd>
				<dt>URL</dt>
				<dd><p>${url}</dd>
				<dt>Exception</dt>
				<dd><p>${exception}</dd>
			</dl>
			
			<h3>Stack Trace</h3>
			<pre style='overflow:auto;'><c:out value="${exceptionTrace}" /></pre>
			
			<h3>Page Parameters</h3>
			<c:forEach items='${param}' var='h'>
			   <dl>
			      <dt><c:out value='${h.key}'/></dt>
			      <dd><c:out value='${h.value}'/></dd>
			   </dl>
			</c:forEach>
			
			<h3>Request Headers</h3>
			<c:forEach items='${header}' var='h'>
			   <dl>
			      <dt><c:out value='${h.key}'/></dt>
			      <dd><c:out value='${h.value}'/></dd>
			   </dl>
			</c:forEach>
			
			<h3>Page Scope</h3>
			<c:forEach items='${pageScope}' var='h'>
			   <dl>
			      <dt><c:out value='${h.key}'/></dt>
			      <dd><c:out value='${h.value}'/></dd>
			   </dl>
			</c:forEach>
			
			<h3>Request Scope</h3>
			<c:forEach items='${requestScope}' var='h'>
			   <dl>
			      <dt><c:out value='${h.key}'/></dt>
			      <dd><c:out value='${h.value}'/></dd>
			   </dl>
			</c:forEach>
			
			<h3>Session Scope</h3>
			<c:forEach items='${sessionScope}' var='h'>
			   <dl>
			      <dt><c:out value='${h.key}'/></dt>
			      <dd><c:out value='${h.value}'/></dd>
			   </dl>
			</c:forEach>
		</div>
	</div>
</c:if>