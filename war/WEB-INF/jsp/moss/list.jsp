<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div style="float: left">
	<button style="float: left; text-align: center;"
		onclick="location.href='../../run/${assessmentName}/'">Run MOSS</button>
</div>

<c:forEach var="moss" items="${mossList}">
	<h2><a href="${moss.key}/">${moss.value}</a></h2>
</c:forEach>
