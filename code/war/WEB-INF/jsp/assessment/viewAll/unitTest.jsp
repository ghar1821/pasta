<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Unit Tests</h1>

<table class="pastaTable">
	<th><td>Name</td><td>Tested</td></th>
	<c:forEach var="unitTest" items="${allUnitTests}>
		<tr><td>${unitTest.name}</td><td>${unitTest.tested}</td></tr>
	</c:forEach>
</table>