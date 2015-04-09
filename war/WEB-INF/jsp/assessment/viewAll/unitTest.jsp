<!-- 
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
-->

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1> Unit Tests</h1>

<table class="pastaTable">
	<c:forEach var="unitTest" items="${allUnitTests}">
		<tr>		
			<td class="pastaTF pastaTF${unitTest.tested}">
				<!-- status -->
				<c:choose>
					<c:when test="${unitTest.tested}">
						TESTED
					</c:when>
					<c:otherwise>
						UNTESTED
					</c:otherwise>
				</c:choose>
			</td>
			<td>
				<!-- name -->
				<b>${unitTest.name}</b>
			</td>
			<td>
				<!-- buttons -->
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="location.href='./${unitTest.id}/'">Details</button>
				</div>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
					<button style="float:left; display:none; text-align: center; " onclick="location.href='./delete/${unitTest.id}/'" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
				</div>
			</td>
		</tr>
	</c:forEach>
</table>

<button id="newPopup">Add a new Unit Test</button>

<div id="newUnitTestDiv" class='popup' >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> New Unit Test </h1>
	<form:form commandName="newUnitTest" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Unit Test Name:</td><td><form:input autocomplete="off" type="text" path="name"/></td></tr>
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
            $('#newPopup').on('click', function(e) {

                // Prevents the default action to be triggered. 
                e.preventDefault();

                // Triggering bPopup when click event is fired
                $('#newUnitTestDiv').bPopup();

            });
            
        });

    })(jQuery);
</script>
