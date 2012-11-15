<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Unit Tests</h1>

<table class="pastaTable">
	<tr><th>Name</th><th>Tested</th><th><h1>Delete</h1></th></tr>
	<c:forEach var="unitTest" items="${allUnitTests}">
		<tr>
			<td><a href="../view/${unitTest.shortName}/">${unitTest.name}</a></td>
			<td class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</td>
			<td><button onClick="location.href='../delete/${unitTest.shortName}/'">X</button></td>
		</tr>
	</c:forEach>
</table>

<button id="newPopup">Add a new Unit Test</button>

<div id="newUnitTest" >
<span class="button bClose">
	<span><b>X</b></span>
</span>
	<h1> New Unit Test </h1>
	<form:form commandName="newUnitTestModel" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Unit Test Name:</td><td><form:input type="text" path="testName" value=""/></td></tr>
			<tr><td>Unit Test Code:</td><td><form:input type="file" path="file"/></td></tr>
		</table>
    	<input type="submit" value="Create" id="submit"/>
	</form:form>
</div>

<script>
	;(function($) {

         // DOM Ready
        $(function() {

            // Binding a click event
            // From jQuery v.1.7.0 use .on() instead of .bind()
            $('#newPopup').bind('click', function(e) {

                // Prevents the default action to be triggered. 
                e.preventDefault();

                // Triggering bPopup when click event is fired
                $('#newUnitTest').bPopup();

            });

        });

    })(jQuery);
</script>