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
