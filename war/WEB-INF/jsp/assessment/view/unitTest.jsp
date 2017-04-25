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

<h1>Unit Test - ${unitTest.name}</h1>
<h4>Click <a href='../../help/unitTests/'>here</a> for help with unit tests.</h4>

<form:form commandName="updateUnitTest" enctype="multipart/form-data" method="POST">
	<div class='section'>
		<h2 class='section-title'>Details</h2>
		<div class='part'>
			<div class='pasta-form'>
				<div class='pf-section'>
					<div class='pf-item one-col'>
						<div class='pf-label'>Name</div>
						<div class='pf-input'>
							<form:input path="name" />
							<form:errors path="name" />
						</div>
					</div>
					<div class='pf-item one-col'>
						<div class='pf-label'>Submission base directory <span class='help'>The expected base directory of students' code (where the root is their code submission); e.g. "<code>src</code>". If their code is not to be submitted in a directory, leave this blank.</span></div>
						<div class='pf-input'>
							<form:input path="submissionCodeRoot"/>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class='section'>
		<h2 class='section-title'>Input-Output Testing</h2>
		<div class='part no-line'>
			Define inputs and (if desired) expected outputs from the submission under test.
		</div>
		<div id="allTestCases" class='part no-line'>
			<h3 class='part-title'>Tests</h3>
			<form:errors path="testCases" element="div" cssClass="vertical-block" />
			<c:if test="${empty updateUnitTest.testCases}">
				<span id='noBlackBox'>Click "Add New Test" to create a test case.</span>
			</c:if>
			<c:forEach var="testCase" items="${updateUnitTest.testCases}" varStatus="testStatus">
				<div class='testCase'>
					<form:hidden path="testCases[${testStatus.index}].id"/>
					<div class='controls float-right'>
						<a class='copyCase'><span class='fa fa-lg fa-files-o' title='Copy'></span></a>
						<form:hidden path="testCases[${testStatus.index}].deleteMe"/>
						<a class='deleteCase'><span class='fa fa-lg fa-trash-o' title='Delete'></span></a>
					</div>
					
					<div class='pasta-form wide'>
						<div class='pf-item one-col'>
							<div class='pf-label'>Test Name</div>
							<div class='pf-input'>
								<form:input path="testCases[${testStatus.index}].testName"/>
								<form:errors path="testCases[${testStatus.index}].testName" />
							</div>
						</div>
						<div class='pf-item one-col'>
							<div class='pf-label'>Timeout (ms)</div>
							<div class='pf-input'>
								<form:input path="testCases[${testStatus.index}].timeout"/>
								<form:errors path="testCases[${testStatus.index}].timeout" />
							</div>
						</div>
						<div class='pf-item one-col'>
							<div class='pf-label'>Command Line Arguments</div>
							<div class='pf-input'>
								<form:input path="testCases[${testStatus.index}].commandLine" cssClass="code" />
							</div>
						</div>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Console Input</div>
								<div class='pf-input'>
									<form:textarea cssClass="console" path="testCases[${testStatus.index}].input"/>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Preview (showing whitespace)</div>
								<div class='pf-input'>
									<div class='consolePreview'></div>
								</div>
							</div>
						</div>
						<div class='pf-item'>
							<form:checkbox cssClass='toggleOutput' path="testCases[${testStatus.index}].toBeCompared" label="Compare user output"/>
						</div>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Expected Console Output</div>
								<div class='pf-input'>
									<form:textarea cssClass="console" path="testCases[${testStatus.index}].output"/>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Preview (showing whitespace)</div>
								<div class='pf-input'>
									<div class='consolePreview'></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</c:forEach>
			<div id='emptyTest' class='hidden'>
				<div class='testCase'>
					<div class='controls float-right'>
						<a class='copyCase'><span class='fa fa-lg fa-files-o' title='Copy'></span></a>
						<input id="testCases0.deleteMe" name="testCases[0].deleteMe" type="hidden" value="false"/>
						<a class='deleteCase'><span class='fa fa-lg fa-trash-o' title='Delete'></span></a>
					</div>
					
					<div class='pasta-form wide'>
						<div class='pf-item one-col'>
							<div class='pf-label'>Test Name</div>
							<div class='pf-input'>
								<input id="testCases0.testName" name="testCases[0].testName" type="text" value=""/>
							</div>
						</div>
						<div class='pf-item one-col'>
							<div class='pf-label'>Timeout (ms)</div>
							<div class='pf-input'>
								<input id="testCases0.timeout" name="testCases[0].timeout" type="text" value="2000"/>
							</div>
						</div>
						<div class='pf-item one-col'>
							<div class='pf-label'>Command Line Arguments</div>
							<div class='pf-input'>
								<input id="testCases0.commandLine" name="testCases[0].commandLine" class="code" type="text" value="" />
							</div>
						</div>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Console Input</div>
								<div class='pf-input'>
									<textarea id="testCases0.input" name="testCases[0].input" class="console"></textarea>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Preview (showing whitespace)</div>
								<div class='pf-input'>
									<div class='consolePreview'></div>
								</div>
							</div>
						</div>
						<div class='pf-item'>
							<input id="testCases0.toBeCompared1" name="testCases[0].toBeCompared" class="toggleOutput" type="checkbox" value="true" checked="checked">
							<label for="testCases0.toBeCompared1">Compare user output</label>
							<input type="hidden" name="_testCases[0].toBeCompared" value="on">
						</div>
						<div class='pf-horizontal two-col'>
							<div class='pf-item'>
								<div class='pf-label'>Expected Console Output</div>
								<div class='pf-input'>
									<textarea id="testCases0.output" name="testCases[0].output" class="console"></textarea>
								</div>
							</div>
							<div class='pf-item'>
								<div class='pf-label'>Preview (showing whitespace)</div>
								<div class='pf-input'>
									<div class='consolePreview'></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div id='ioOptions' class='part no-line'>
			<h3 class='part-title'>Options</h3>
			<div class='pasta-form'>
				<div class='pf-section'>
					<div class='pf-item'>
						<form:checkbox path="blackBoxOptions.detailedErrors" label="Detailed error messages"/>
						<span class='help'>With this option, users will see messages displaying the difference between their output and the expected output. Otherwise they will just see whether they were correct or not.</span>
					</div>
				</div>
				<div class='pf-section'>
					<div class='pf-item one-col'>
						<div class='pf-label'>GCC Command Line Arguments (C submissions only)</div>
						<div class='pf-input'>
							<form:input path="blackBoxOptions.gccCommandLineArgs" cssClass="code"/>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class='button-panel'>
			<button type='submit'>Save Changes</button>
			<button id='addTest' class='secondary'>Add New Test</button>
		</div>
	</div>
	
	<div class='section'>
		<h2 class='section-title'>Accessory Files</h2>
		<div class='part no-line'>
			These files will be available to any input-output or custom tests when they are run, and will be readable by student submissions. Files are available from the same directory as running code.
		</div>
		<div class='part no-line'>
			<div class='pasta-form'>
				<div class='pf-section'>
					<div class='pf-horizontal two-col'>
						<div class='pf-item'>
							<div class='pf-label'>Upload New Files</div>
							<div class='pf-input'>
								<form:input type="file" path="accessoryFile"/>
							</div>
						</div>
						<c:if test="${unitTest.hasAccessoryFiles}">
							<div class='pf-item'>
								<div class='pf-label'>Current Accessory Files <a id='del-accessory'>(Delete)</a></div>
								<div class='pf-input'>
									<c:set var="node" value="${accessoryNode}" scope="request"/>
									<jsp:include page="../../recursive/fileWriterRoot.jsp">
										<jsp:param name="owner" value="unitTest"/> 
										<jsp:param name="fieldId" value="${unitTest.id}"/> 
									</jsp:include>
								</div>
							</div>
						</c:if>
					</div>
					<c:if test="${unitTest.hasAccessoryFiles}">
						<div class='pf-item'>
							<form:checkbox path="allowAccessoryWrite" label="Allow students to write to accessory files"/>
						</div>
					</c:if>
				</div>
			</div>
		</div>
		<div class='button-panel'>
			<button type='submit'>Save Changes</button>
		</div>
	</div>
	
	<div class='section'>
		<h2 class='section-title'>Advanced Testing</h2>
		<div class='part no-line'>
			Use these tests to describe unit tests that are more complicated than just simple output matching. These tests will be run directly after any input-output tests are run, and will have access to submission outputs.
		</div>
		<div class='part no-line'>
			<h3 class='part-title'>Custom JUnit Test Code</h3>
			<div class='pasta-form'>
				<div class='pf-section'>
					<div class='pf-horizontal two-col'>
						<div class='pf-item'>
							<div class='pf-label'>Upload Code</div>
							<div class='pf-input'>
								<form:input type="file" path="file"/>
							</div>
						</div>
						<c:if test="${unitTest.hasCode}">
							<div class='pf-item'>
								<div class='pf-label'>Current Code <a id='del-code'>(Delete)</a></div>
								<div class='pf-input'>
									<c:set var="node" value="${codeNode}" scope="request"/>
									<jsp:include page="../../recursive/fileWriterRoot.jsp">
										<jsp:param name="owner" value="unitTest"/> 
										<jsp:param name="fieldId" value="${unitTest.id}"/> 
									</jsp:include>
								</div>
							</div>
						</c:if>
					</div>
					<c:if test="${unitTest.hasCode}">
						<div class='pf-item'>
							<div class='pf-label'>Main class</div>
							<div class='pf-input'>
								<form:select path="mainClassName">
									<form:option value="" label="--- Select ---"/>
									<form:options items="${candidateMainFiles}" />
								</form:select>
							</div>
						</div>
					</c:if>
				</div>
			</div>
		</div>
		<c:if test="${user.instructor}">
			<div class='button-panel'>
				<button type='submit'>Save Changes</button>
				<c:if test="${unitTest.hasCode}">
					<a href="./download/"><button class='flat' id="downloadTest">Download Test Code</button></a>
				</c:if>
			</div>
		</c:if>
	</div>
</form:form>

<c:if test="${not empty latestResult.testCases and not unitTest.tested}">
	<c:set var="showMarkAsWorking" value="true" />
</c:if>
<c:if test="${(not unitTest.hasCode and unitTest.hasBlackBoxTests) or (unitTest.hasCode and not empty unitTest.mainClassName)}">
	<c:set var="showTest" value="true" />
</c:if>
<c:if test="${showMarkAsWorking or showTest}">
	<div id='checkWorking' class='section section-above'>
		<h2 class='section-title'>Check that everything works</h2>
		<div class='part no-line'>
			Before using this unit test module in an assessment, it is advisable that you run a sample submission first. Upload a submission and confirm that it works here.
		</div>
		<div class='part no-line'>
			<div class='past-form'>
				<div class='pf-item'>
					<div class='pf-label'>Unit test confirmed as working:</div>
					<div class='pf-input'>
						<span class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</span>
					</div>
				</div>
			</div>
		</div>
		<div class='part'>
			<div class='button-panel'>
				<c:if test="${showMarkAsWorking}">
					<button id="acceptUnitTest">Mark as working and tested</button>
				</c:if>
				<c:if test="${showTest}">
					<button ${(unitTest.tested or showMarkAsWorking) ? "class='flat'" : ""} id="testPopup"> Test Unit Test </button>
				</c:if>
			</div>
		</div>
	</div>
</c:if>

<c:if test="${not empty latestResult}">
	<div class='section'>
		<c:if test="${not empty latestResult}">
			<h2 class='section-title'>Latest Test results
				<c:choose>
					<c:when test="${latestResult.error}">
						- ${latestResult.errorReason}
					</c:when>
					<c:otherwise>
						- Execution Successful
					</c:otherwise>
				</c:choose>
			</h2>
			
			<c:if test="${not empty latestResult.filesCompiled}">
				<div class='part no-line'>
					<h3 class='part-title'>Files Compiled:</h3>
					<div class="ui-state-highlight">
						<pre>${latestResult.filesCompiled}</pre>
					</div>
				</div>
			</c:if>
			<c:if test="${latestResult.validationError}">
				<div class='part no-line'>
					<h3 class='part-title'>Validation Errors:</h3>
					<div class="ui-state-error">
						<c:forEach var="errorList" items="${latestResult.validationErrorsMap}">
							<c:if test="${not empty errorList.key}">
								<p><strong><c:out value="${errorList.key}" /></strong>
							</c:if>
							<ul>
								<c:forEach var="error" items="${errorList.value}">
									<li><c:out value="${error}" />
								</c:forEach>
							</ul>
						</c:forEach>
					</div>
				</div>
			</c:if>
			<c:if test="${not empty latestResult.compileErrors}">
				<div class='part no-line'>
					<h3 class='part-title'>Compile Errors:</h3>
					<div class="ui-state-error">
						<pre>${latestResult.compileErrors}</pre>
					</div>
				</div>
			</c:if>
			<c:if test="${not empty latestResult.runtimeErrors}">
				<div class='part no-line'>
					<h3 class='part-title'>Runtime Errors:</h3>
					<div class="ui-state-error">
						<pre>${latestResult.runtimeErrors}</pre>
					</div>
				</div>
			</c:if>
			<c:if test='${not empty latestResult.fullOutput}'>
				<c:choose>
					<c:when test="${latestResult.error}">
						<div class='part no-line'>
							<h3 class='part-title'>Full Output:</h3>
							<div class="ui-state-error">
								<pre>${latestResult.fullOutput}</pre>
							</div>
						</div>
					</c:when>
					<c:otherwise>
						<div class='button-panel'>
							<button class='flat' id="showFullOutput">Show full output</button>
						</div>
						<div id='fullOutputPopup' class='popup'>
							<h3 class='part-title'>Full Output:</h3>
							<div class="ui-state-highlight largeOutputBox">
								<pre>${latestResult.fullOutput}</pre>
							</div>
						</div>
					</c:otherwise>
				</c:choose>
			</c:if>
			<c:if test="${not empty latestResult.testCases}">
				<div class='part no-line'>
					<h3 class='part-title'>Test Results</h3>
					<div class='part no-line float-container'>
						<c:forEach var="unitTestCase" items="${latestResult.testCases}">
							<div class="unitTestResult ${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp;</div>
						</c:forEach>
					</div>
					<div class='part no-line' style='overflow:hidden;'>
						<table id='testResults'>
							<thead>
								<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
							</thead>
							<tbody>
								<c:forEach var="testCase" items="${latestResult.testCases}">
									<tr>
										<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
										<td style="text-align:left;">${testCase.testName}</td>
										<td>${testCase.time}</td>
										<td>
											<pre><c:if test="${not testCase.failure}">${testCase.type} - </c:if><c:out value="${testCase.testMessage}" /></pre>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</c:if>
		</c:if>
	</div>
</c:if>

<div id="testUnitTestDiv" class='popup'>
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> Test Unit Test </h1>
	<form:form commandName="testUnitTest" action="./test/" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Test Submission:</td><td><form:input type="file" path="file"/> <form:errors path="file" /></td></tr>
			<tr>
				<td>Solution Name:</td>
				<td>
					<form:input path="solutionName"/>
					<span class='help'>The name of the main solution source code file (if you are using black box tests). If students are to submit <code>MyProgram.java</code> and <code>MyProgram.c</code>, then solution name should be "MyProgram"</span>
					<form:errors path="solutionName" />
				</td>
			</tr>
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
                e.preventDefault();
                $('#testUnitTestDiv').bPopup();
            });
            <spring:hasBindErrors name='testUnitTest'>
            	$('#testUnitTestDiv').bPopup();
       		</spring:hasBindErrors>
            
            $('#showFullOutput').on('click', function(e) {
                e.preventDefault();
                $('#fullOutputPopup').bPopup();
            });
            
            $('#acceptUnitTest').on('click', function(e) {
                e.preventDefault();
                $('#confirmPopup').bPopup();
            });
            
            $('#downloadTest').on('click', function(e) {
                e.preventDefault();
            });
            
            <%-- Disable test code button when main class not selected --%>
            $('#mainClassName').on('change', function() {
            	$('#testPopup').prop('disabled', !$(this).find(':selected').val());
            });
            
            $("#del-code").on("click", function() {
            	var confirmResult = confirm("Are you sure you wish to delete the code for this unit test?");
            	if(confirmResult) {
            		$("<form />", {
            			action : "clearCode/",
            			method : "post"
            		})
            		.appendTo('body')
            		.submit();
            	}
            });
            $("#del-accessory").on("click", function() {
            	var confirmResult = confirm("Are you sure you wish to delete the accessory files for this unit test?");
            	if(confirmResult) {
            		$("<form />", {
            			action : "clearAccessory/",
            			method : "post"
            		})
            		.appendTo('body')
            		.submit();
            	}
            });
            
            $("#addTest").on("click", function(e) {
            	e.preventDefault();
            	addTest();
            });
            $(document).on("click", ".copyCase", function(e){
            	addTest($(this).closest(".clb-content"));
            });
            function addTest($baseTest) {
            	$("#noBlackBox").remove();
            	var newIndex = $("#allTestCases").children(".testCase").length;
            	if(!$baseTest) {
            		$baseTest = $("#emptyTest").children().first();
            	}
            	$baseTest = $baseTest.clone();
            	if(newIndex > 0) {
	            	$baseTest.find("input,textarea").each(function() {
	            		$(this).attr("name", $(this).attr("name").replace(/[0-9]+/g, newIndex));
	            		if($(this).attr("id")) {
	            			$(this).attr("id", $(this).attr("id").replace(/[0-9]+/g, newIndex));
	            		}
	            	});
	            	$baseTest.find("label").each(function() {
	            		$(this).attr("for", $(this).attr("for").replace(/[0-9]+/g, newIndex));
	            	});
            	}
            	$baseTest.insertBefore($("#emptyTest"));
            	$baseTest.find("textarea.console").allowTabChar();
            	$baseTest.find("input[name$='.id']").remove();
            	
            	$baseTest.collapsible({
            		"heading-selector": ".pf-item:first,.controls"
            	});
            	if($baseTest.is(".clb-content")) {
            		$baseTest.toggleClass("clb-content testCase");
            	}
<<<<<<< Upstream, based on origin/master
            	
            	$baseTest.find("input[name$='.testName']").focus().select();
=======
>>>>>>> 8646720 Fixed bug where you can't delete copied test cases.
            }
            
            $(document).on("click", "input.toggleOutput", function() {
            	toggleOutput($(this));
            });
            $("input.toggleOutput").each(function() {
            	toggleOutput($(this));
            });
            
            $("form").on("submit", function() {
            	$("#emptyTest").empty();
            	$('.console').each(function() {
            		$(this).val($(this).val().replace(/\r/g,""));
            	})
            });
            
         	// controls for the "delete case" button
        	$(document.body).on('click', 'a.deleteCase', function() {
        		var $container = $(this).closest(".testCase");
        		var collapsibleWrapper = $container.children(".clb-content-wrapper");
        		var deleting = !collapsibleWrapper.is(".toDelete");
        		collapsibleWrapper.toggleClass("toDelete");
        		
        		$(this).children().first().toggleClass("fa-trash-o fa-undo");
        		$(".fa-trash-o").attr("title", "Delete");
        		$(".fa-undo").attr("title", "Undo Delete");
        		$(this).prev().val(deleting);
        		
        		$container.find("input[type!='hidden'],textarea").prop("disabled", deleting);
        		
        		if(!$container.is(".collapsed")) {
        			$container.collapse();
        		}
        		$(this).next().toggle(!deleting);
        	});
         	
         	$(document.body).on("keyup", "textarea.console", function() {
         		updateConsolePreviews($(this));
         	});
        	$(".console").allowTabChar();
         	$(".console").each(function() {
         		updateConsolePreviews($(this));
         	});
         	
         	tabSize = findTabWidth($(".consolePreview").first());
         	
         	$("#testResults").addClass("stripe row-border hover").DataTable({
    			retrieve: true,
    			"searching" : false,
    			"paging" : false,
    			"info" : false,
    			language : {
    				emptyTable: "No unit test cases to display."
    			}
    		});
         	
         	$("#ioOptions").collapsible({
         		collapsed: true,
         		"heading-selector": "h3"
         	});
         	$(":not(#emptyTest) > .testCase").collapsible({
         		collapsed: true,
         		"heading-selector": ".pf-item:first,.controls"
         	});
         	
  			<c:if test="${not empty sessionScope.ts}">
  				<c:remove var="ts" scope="session" />
  				$("#checkWorking")[0].scrollIntoView(true);
  			</c:if>
  			
         	<spring:hasBindErrors name='updateUnitTest'>
         		$("[id$='.errors']").closest(".testCase").expand();
         		var offset = $("[id$='.errors']").first().closest(".pf-item").offset();
				offset.left -= 20;
				offset.top -= 20;
				$('html, body').animate({
				    scrollTop: offset.top,
				    scrollLeft: offset.left
				});
	     	</spring:hasBindErrors>
        });
        
        function toggleOutput($checkbox) {
        	var $outputTR = $checkbox.closest(".pf-item").next();
        	$outputTR.find("textarea").prop("disabled", !$checkbox.is(":checked"));
        	$outputTR.toggle($checkbox.is(":checked"));
        }
         
        function updateConsolePreviews($console) {
        	var $preview = $console.closest(".pf-horizontal").find(".consolePreview");
     		var text = $console.val();
     		text = text.replace(/\r/g, "");
     		text = text.replace(/ /g, "&middot;");
     		text = replaceTabs(text);
     		text = text.replace(/&(?!middot;|not;)/g, "&amp;");
     		text = text.replace(/\n/g, "&para;\n");
     		text = text.replace(/</g, "&lt;");
     		text = text.replace(/>/g, "&gt;");
     		$preview.html(text);
        }
         
        var tabSize = 8;
        function findTabWidth($element) {
        	var width = $element.css("tab-size");
        	if(!width) {
        		width = $element.css("-moz-tab-size"); /* Code for Firefox */
        	}
        	if(!width) {
        		width = $element.css("-o-tab-size"); /* Code for Opera 10.6-12.1 */
        	}
        	if(!width) {
        		width = 8; /* Code for IE */
        	}
        	return width;
        }
        
        function replaceTabs(text) {
        	var lines = text.split('\n');
        	for(var j = 0; j < lines.length; j++) {
        		var bits = lines[j].split('\t');
            	var wholeTab = Array(Number(tabSize) + 1).join(' ');
            	var newText = "";
            	for(var i = 0; i < bits.length - 1; i++) {
            		var length = bits[i].replace(/&middot;/g," ").length;
            		var thisTab = tabSize - (length % tabSize);
            		var left = wholeTab.substr(0, thisTab / 2);
            		var right = wholeTab.substr(0, thisTab - 1 - left.length);
            		bits[i] = bits[i] + left + "&not;" + right;
            	}
            	lines[j] = bits.join('');
        	}
        	return lines.join('\n');
        }
    })(jQuery);
</script>