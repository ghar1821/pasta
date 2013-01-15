<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
	<c:when test="${not node.leaf}">
		<li class="list">
			<span style="float:left;" class="ui-icon ui-icon-folder-open"></span>
			${node.name}
		</li>
		<ul class="list">
			<c:forEach var="node" items="${node.children}">
				<c:set var="node" value="${node}" scope="request"/>
				<jsp:include page="fileWriter.jsp"/>
			</c:forEach>
		</ul>
	</c:when>
	<c:otherwise>
		<li class="list">
			<form action="../../../../viewFile/" method="post" target="_blank">
				<input type="hidden" name="location" value="${node.location}"/>
			    <div style="cursor:pointer" onclick="this.parentNode.submit()">
			    	<span style="float:left;" class="ui-icon ui-icon-document"></span>
			    	${node.name}
			    </div>
			</form>
		</li>
	</c:otherwise>
</c:choose>
