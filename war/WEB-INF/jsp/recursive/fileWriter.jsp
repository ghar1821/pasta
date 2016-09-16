<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose>
	<c:when test="${not node.leaf}">
		<li class='directory'>
			<span onclick="$(this).parents().first().toggleClass('directory expanded');$(this).next().toggle('fast');">${node.name}</span>
			<ul class="jqueryFileTree" style="display:none">
				<c:forEach var="node" items="${node.children}">
					<c:set var="node" value="${node}" scope="request"/>
					<jsp:include page="fileWriter.jsp"/>
				</c:forEach>
			</ul>
		</li>
	</c:when>
	<c:otherwise>
		<li class="leaf ext_${node.extension}" location="${node.location}" owner="${param.owner}" fieldId="${param.fieldId}"><a>${node.name}</a></li>
	</c:otherwise>
</c:choose>
