<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>${competition.name}</h1>

<c:choose>
	<c:when test="${empty marks}">
		No results available
	</c:when>
	<c:otherwise>
		<table>
			<tr>
				<th>Position</th><th>Username</th><th>Percentage</th>
				<c:choose>
					<c:when test="${not unikey.tutor}">
						<c:forEach var="category" items="${arenaResult.studentVisibleCategories}">
							<th>${category}</th>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<c:forEach var="category" items="${arenaResult.categories}">
							<th>${category}</th>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</tr>
			<c:forEach var="positionData" items="${marks.positions}">
				<c:forEach var="compResult" items="${positionData.value}">
					<tr>
						<td>
							${positionData.key}
							<c:if test="${fn:length(positionData.value) > 1}">
								=
							</c:if>
						</td>
						<td>${compResult.username}</td>
						<td>${compResult.percentage}</td>
						<c:choose>
							<c:when test="${not unikey.tutor}">
								<c:forEach var="category" items="${arenaResult.studentVisibleCategories}">
									<td>${arenaResult.data[compResult.username][category]}</td>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<c:forEach var="category" items="${arenaResult.categories}">
									<td>${arenaResult.data[compResult.username][category]}</td>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>
