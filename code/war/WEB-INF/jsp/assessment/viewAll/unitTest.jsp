<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Unit Tests</h1>

<table class="pastaTable">
	<tr><th>Name</th><th>Tested</th></tr>
	<c:forEach var="unitTest" items="${allUnitTests}">
		<tr><td><a href="../view/${unitTest.shortName}/">${unitTest.name}</a></td><td class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</td></tr>
	</c:forEach>
</table>