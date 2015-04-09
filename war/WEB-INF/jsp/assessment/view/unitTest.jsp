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

<h1> Unit Test - ${unitTest.name}</h1>

<form:form commandName="updateUnitTest" enctype="multipart/form-data" method="POST">
	<table>
		<tr><td>Name:</td><td><form:input path="name" /></td></tr>
		<tr><td>Has been tested:</td><td class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</td></tr>
		
		<tr><td>Upload Code:</td><td><form:input type="file" path="file"/></td></tr>
		<c:if test="${unitTest.hasCode}">
			<tr>
				<td>Current Code</td>
				<td>
					<ul class="list">
					<jsp:include page="../../recursive/fileWriter.jsp"/>
					</ul>
				</td>
			</tr>
			<tr>
				<td>Main class:</td>
				<td>
					<form:select path="mainClassName">
						<form:option value="" label="--- Select ---"/>
						<form:options items="${candidateMainFiles}" />
					</form:select>
				</td>
			</tr>
		</c:if>
		<tr><td></td><td><input type="submit" value="Save Changes" id="submit" /></td></tr>
	</table> 
</form:form>

<c:if test="${not empty unitTest.mainClassName}">
	<button id="testPopup"> Test Unit Test </button>
</c:if>
<c:if test="${unitTest.hasCode}">
	<a href="./download/"><button id="downloadTest"> Download Test </button></a>
</c:if>

<c:if test="${not empty latestResult}">
	<h2>Latest Test results
		<c:if test="${not empty latestResult.compileErrors}">
			- Compilation Errors Detected
		</c:if>
		<c:if test="${not empty latestResult.testCases}">
			- Execution Successful
		</c:if>
	</h2>
	<c:if test="${not empty latestResult.compileErrors}">
		<div class="ui-state-error">
			<pre>${latestResult.compileErrors}</pre>
		</div>
	</c:if>
	<c:if test="${not empty latestResult.runtimeOutput}">
		<div class="ui-state-error">
			<pre>${latestResult.runtimeOutput}</pre>
		</div>
	</c:if>
	<c:if test="${not empty latestResult.testCases}">
		<div>
		<c:forEach var="testCase" items="${latestResult.testCases}">
				<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${testCase.testResult}" title="${testCase.testName}">&nbsp;</div>
		</c:forEach>
		</div>
		<br />
		<button id="acceptUnitTest">Working as intended</button>
		<table class="pastaTable">
			<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
			<c:forEach var="testCase" items="${latestResult.testCases}">
				<tr>
					<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
					<td style="text-align:left;">${testCase.testName}</td>
					<td>${testCase.time}</td>
					<td>
						<pre>${testCase.type} - ${testCase.testMessage}</pre>
					</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</c:if>

<div id="testUnitTestDiv" class='popup'>
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> Test Unit Test </h1>
	<form:form commandName="testUnitTest" action="./test/" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Unit Test Code:</td><td><form:input type="file" path="file"/></td></tr>
		</table>
    	<input type="submit" value="Upload" id="submit"/>
	</form:form>
</div>

<div id="confirmPopup" class='popup' >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1>Are you sure you want to do that?</h1>
	<a href="../tested/${unitTest.id}/"><button id="confirmButton">Confirm</button></a>
</div>

<script>
	;(function($) {

         // DOM Ready
        $(function() {

            $('#testPopup').on('click', function(e) {
                // Prevents the default action to be triggered. 
                e.preventDefault();
                $('#testUnitTestDiv').bPopup();
            });
            
            $('#acceptUnitTest').on('click', function(e) {
                // Prevents the default action to be triggered. 
                e.preventDefault();
                $('#confirmPopup').bPopup();
            });
            
            <%-- Disable test code button when main class not selected --%>
            $('#mainClassName').on('change', function() {
            	$('#testPopup').prop('disabled', !$(this).find(':selected').val());
            });

        });

    })(jQuery);
</script>