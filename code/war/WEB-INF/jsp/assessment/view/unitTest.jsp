<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
		<script type="text/javascript">
$(document).ready( function() {
    $('#filetree1').fileTree({ root: '' }, script: 'connectors/jqueryFileTree.jsp', function(file) {
        alert(file);
    });
});
</script>
<h1> Unit Test - ${unitTest.name}</h1>
<div id = "filetree1" name = "filetree1"></div>
<table>
	<tr><td>Has been tested:</td><td class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</td></tr>
</table> 

<!-- show files in folder TODO #42 -->

		
<c:out value="${testPath}"/>
<!-- Upload testing submission TODO #43 -->