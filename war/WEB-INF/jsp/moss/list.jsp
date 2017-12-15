<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class='vertical-block'>
	<button onclick="location.href='../../run/${assessmentId}/'">Run MOSS</button>
</div>

<div class='vertical-block'>
	<c:forEach var="moss" items="${mossList}">
		<h2>
			<a href="${moss.key}/">${moss.value}</a>
		</h2>
	</c:forEach>
</div>
