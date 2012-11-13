<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Unit Test - ${unitTest.name}</h1>

<table>
	<tr><td>Is secret:</td><td class="pastaTF pastaTF${unitTest.secret}">${unitTest.secret}</td></tr>
	<tr><td>Has been tested:</td><td class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</td></tr>
</table> 

<!-- show files in folder TODO #42 -->