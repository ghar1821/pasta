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
	<table class='vertical-block'>
		<tr><td><strong>Compatible languages:</strong></td><td>
			<c:choose>
				<c:when test="${unitTest['class'].simpleName == 'UnitTest'}">
					Java
				</c:when>
				<c:when test="${unitTest['class'].simpleName == 'BlackBoxTest'}">
					Java, Python, C, C++, Octave
				</c:when>
				<c:otherwise>
					None
				</c:otherwise>
			</c:choose><span class='help'>If an assessment is to run student submissions as code using this test, these are the languages that can be run using this test type.</span>
		</td></tr>
		<tr><td colspan='2'><hr /></td></tr>
		<tr><td>Name:</td><td><form:input path="name" /> <form:errors path="name" /></td></tr>
		<tr><td>Has been tested:</td><td class="pastaTF pastaTF${unitTest.tested}">${unitTest.tested}</td></tr>
		
		<c:if test="${unitTest['class'].simpleName == 'UnitTest'}">
			<tr><td>Upload Code:</td><td><form:input type="file" path="file"/></td></tr>
			<c:if test="${unitTest.hasCode}">
				<tr>
					<td>Current Code</td>
					<td>
						<jsp:include page="../../recursive/fileWriterRoot.jsp"/>
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
				<tr>
					<td>Submission base directory:</td>
					<td>
						<form:input path="submissionCodeRoot"/> <span class='help'>The expected base directory of students' code (where the root is their code submission); e.g. "<code>src</code>". If their code is not to be submitted in a directory, leave this blank.</span>
					</td>
				</tr>
			</c:if>
		</c:if>
		<tr><td></td><td><input type="submit" value="Save Changes" id="submit" /></td></tr>
	</table> 
	<c:if test="${unitTest['class'].simpleName == 'BlackBoxTest'}">
		<div id='allTestCases' class='vertical-block'>
			<form:errors path="testCases" element="div" />
			<c:forEach var="testCase" items="${updateUnitTest.testCases}" varStatus="testStatus">
				<div class='testCase boxCard vertical-block'>
					<form:hidden path="testCases[${testStatus.index}].id"/>
					<div class='float-right'>
						<form:hidden path="testCases[${testStatus.index}].deleteMe"/>
						<a class='deleteCase'><span class='icon_delete' title='Delete'></span></a>
						<a class='showHide'><span class='icon_toggle_minus' title='Hide Details'></span></a>
					</div>
					<div class='vertical-block showHide'>
						<p>Test Name: <form:input path="testCases[${testStatus.index}].testName"/> <form:errors path="testCases[${testStatus.index}].testName" />
					</div>
					<div class='vertical-block'>
						<table class='alignCellsTop'>
							<tr>
								<td>Timeout (ms):</td>
								<td><form:input path="testCases[${testStatus.index}].timeout"/> <form:errors path="testCases[${testStatus.index}].timeout" /></td>
								<td></td>
							</tr>
							<tr>
								<td>Command Line Arguments:</td>
								<td><form:textarea path="testCases[${testStatus.index}].commandLine" cols="40" rows="3"/></td>
								<td></td>
							</tr>
							<tr>
								<td>Console Input:</td>
								<td><form:textarea cssClass="console" path="testCases[${testStatus.index}].input" cols="40" rows="7"/></td>
								<td>Preview (showing whitespace):<br/><div class='boxCard'><pre class="consolePreview"></pre></div></td>
							</tr>
							<tr>
								<td>Expected Console Output:</td>
								<td><form:textarea cssClass="console" path="testCases[${testStatus.index}].output" cols="40" rows="7"/></td>
								<td>Preview (showing whitespace):<br/><div class='boxCard'><pre class="consolePreview"></pre></div></td>
							</tr>
						</table>
					</div>
				</div>
			</c:forEach>
			<div id='emptyTest' class='hidden'>
				<div class='testCase boxCard vertical-block'>
					<div class='float-right'>
						<input id="testCases0.deleteMe" name="testCases[0].deleteMe" type="hidden" value="false"/>
						<a class='deleteCase'><span class='icon_delete' title='Delete'></span></a>
						<a class='showHide'><span class='icon_toggle_minus' title='Hide Details'></span></a>
					</div>
					<div class='vertical-block showHide'>
						<p>Test Name: <input id="testCases0.testName" name="testCases[0].testName" type="text" value=""/>
					</div>
					<div class='vertical-block'>
						<table class='alignCellsTop'>
							<tr>
								<td>Timeout (ms):</td>
								<td><input id="testCases0.timeout" name="testCases[0].timeout" type="text" value="2000"/></td>
								<td></td>
							</tr>
							<tr>
								<td>Command Line Arguments:</td>
								<td><textarea id="testCases0.commandLine" name="testCases[0].commandLine" rows="3" cols="40"></textarea></td>
								<td></td>
							</tr>
							<tr>
								<td>Console Input:</td>
								<td><textarea id="testCases0.input" name="testCases[0].input" class="console" rows="7" cols="40"></textarea></td>
								<td>Preview (showing whitespace):<br/><div class='boxCard'><pre class="consolePreview"></pre></div></td>
							</tr>
							<tr>
								<td>Expected Console Output:</td>
								<td><textarea id="testCases0.output" name="testCases[0].output" class="console" rows="7" cols="40"></textarea></td>
								<td>Preview (showing whitespace):<br/><div class='boxCard'><pre class="consolePreview"></pre></div></td>
							</tr>
						</table>
					</div>
				</div>
			</div>
		</div>
		<div class='vertical-block'>
			<button id='addTest'>Add Test</button>
			<input type="submit" value="Save Changes" id="submit" />
		</div>
	</c:if>
</form:form>

<div class='vertical-block'>
	<c:if test="${unitTest['class'].simpleName != 'UnitTest' or not empty unitTest.mainClassName}">
		<button id="testPopup"> Test Unit Test </button>
	</c:if>
	<c:if test="${unitTest['class'].simpleName == 'UnitTest' and unitTest.hasCode}">
		<a href="./download/"><button id="downloadTest"> Download Test </button></a>
	</c:if>
</div>

<c:if test="${not empty latestResult}">
	<h2>Latest Test results
		<c:choose>
			<c:when test="${latestResult.testCrashed}">
				- Unit Test Crashed
			</c:when>
			<c:when test="${not empty latestResult.compileErrors}">
				- Compilation Errors Detected
			</c:when>
			<c:when test="${latestResult.buildError}">
				- Build Error
			</c:when>
			<c:when test="${latestResult.runtimeError}">
				- Runtime Error
			</c:when>
			<c:when test="${latestResult.cleanError}">
				- Cleanup Error
			</c:when>
			<c:otherwise>
				- Execution Successful
			</c:otherwise>
		</c:choose>
	</h2>
	
	<c:if test="${not empty latestResult.filesCompiled}">
		<div class='vertical-block'>
			<h3>Files Compiled:</h3>
			<div class="ui-state-highlight">
				<pre>${latestResult.filesCompiled}</pre>
			</div>
		</div>
	</c:if>
	<c:if test="${not empty latestResult.compileErrors}">
		<div class='vertical-block'>
			<h3>Compile Errors:</h3>
			<div class="ui-state-error">
				<pre>${latestResult.compileErrors}</pre>
			</div>
		</div>
	</c:if>
	<c:choose>
		<c:when test="${latestResult.testCrashed or (empty latestResult.compileErrors and latestResult.buildError) or latestResult.runtimeError or latestResult.cleanError}">
			<div class='vertical-block'>
				<h3>Full Output:</h3>
				<div class="ui-state-error">
					<pre>${latestResult.runtimeOutput}</pre>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<div class='vertical-block'>
				<button id="showFullOutput">Show full output</button>
			</div>
			<div id='fullOutputPopup' class='popup'>
				<h3>Full Output:</h3>
				<div class="ui-state-highlight largeOutputBox">
					<pre>${latestResult.runtimeOutput}</pre>
				</div>
			</div>
		</c:otherwise>
	</c:choose>
	<c:if test="${not empty latestResult.testCases}">
		<div class='vertical-block'>
			<h3>Test Results</h3>
			<div class='vertical-block float-container'>
				<c:forEach var="unitTestCase" items="${latestResult.testCases}">
					<div class="unitTestResult ${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp;</div>
				</c:forEach>
			</div>
			<div class='vertical-block' style='clear:both;'>
				<button id="acceptUnitTest">Working as intended</button>
			</div>
			<div class='vertical-block' style='overflow:hidden;'>
				<table class="pastaTable">
					<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
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
				</table>
			</div>
		</div>
	</c:if>
</c:if>

<div id="testUnitTestDiv" class='popup'>
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> Test Unit Test </h1>
	<form:form commandName="testUnitTest" action="./test/" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Test Submission:</td><td><form:input type="file" path="file"/> <form:errors path="file" /></td></tr>
			<c:if test="${unitTest['class'].simpleName == 'BlackBoxTest'}">
				<tr>
					<td>Solution Name:</td>
					<td>
						<form:input path="solutionName"/>
						<span class='help'>The name of the main solution source code file. If students are to submit <code>MyProgram.java</code> and <code>MyProgram.c</code>, then solution name should be "MyProgram"</span>
						<form:errors path="solutionName" />
					</td>
				</tr>
			</c:if>
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
            <spring:hasBindErrors name='testUnitTest'>
            	$('#testUnitTestDiv').bPopup();
       		</spring:hasBindErrors>
            
            $('#showFullOutput').on('click', function(e) {
                // Prevents the default action to be triggered. 
                e.preventDefault();
                $('#fullOutputPopup').bPopup();
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
            
            $("#addTest").on("click", function(e) {
            	e.preventDefault();
            	var newIndex = $("#allTestCases").children(".testCase").length;
            	var $emptyTest = $("#emptyTest").children().first().clone();
            	if(newIndex > 0) {
	            	$emptyTest.find("input,textarea").each(function() {
	            		$(this).attr("name", $(this).attr("name").replace(/0/g, newIndex));
	            		$(this).attr("id", $(this).attr("id").replace(/0/g, newIndex));
	            	});
            	}
            	$emptyTest.insertBefore($("#emptyTest"));
            	$emptyTest.find("a.showHide").trigger("click");
            });
            
            $("form").on("submit", function() {
            	$("#emptyTest").empty();
            	$('.console').each(function() {
            		$(this).val($(this).val().replace(/\r/g,""));
            	})
            });
            
         	// controls for the "delete case" button
        	$(document.body).on('click', 'a.deleteCase', function() {
        		var $container = $(this).parent().parents("div").first();
        		
        		var deleting = !$container.is(".toDelete");
        		$container.toggleClass("toDelete");
        		
        		$(this).children().first().toggleClass("icon_delete icon_undo");
        		$(".icon_delete").attr("title", "Delete");
        		$(".icon_undo").attr("title", "Undo Delete");
        		$(this).prev().val(deleting);
        		
        		$container.find("input[type!='hidden'],textarea").prop("disabled", deleting);
        		
        		if(!$container.is(".collapsed")) {
        			$(this).next().trigger("click");
        		}
        		$(this).next().toggle(!deleting);
        	});
         	// controls for the "collapse" button
        	$(document.body).on('click', 'a.showHide', function() {
        		var $container = $(this).parent().parents("div").first();
        		
        		$container.toggleClass("collapsed");
        		var collapsed = $container.is(".collapsed");
        		
        		$(this).children().first().toggleClass("icon_toggle_plus icon_toggle_minus");
        		$(".icon_toggle_plus").attr("title", "Show Details");
        		$(".icon_toggle_minus").attr("title", "Hide Details");
        		
        		toggleDetails($container, $(this), collapsed);
        	});
         	// Also collapse on div click
        	$(document.body).on('click', 'div.showHide', function(e) {
        		if(!$(e.target).is("input")) {
        			$(this).prev().find("a.showHide").trigger("click");
        		}
        	});
         	
        	<spring:hasBindErrors name='updateUnitTest'>
				<c:set var='errorsPresent' value='true' />
				var offset = $("[id$='.errors']").first().offset();
				offset.left -= 20;
				offset.top -= 20;
				$('html, body').animate({
				    scrollTop: offset.top,
				    scrollLeft: offset.left
				});
         	</spring:hasBindErrors>
         	<c:if test='${empty errorsPresent}'>
	         	$("a.showHide").trigger("click");
         	</c:if>
 
        	$(".console").allowTabChar();
         	$(".console").each(function() {
         		updateConsolePreviews($(this));
         	});
         	$(".console").on("keyup", function() {
         		updateConsolePreviews($(this));
         	});
         	
         	tabSize = findTabWidth($(".consolePreview").first());
        });
        
        function toggleDetails($container, $link, hiding) {
     		if(hiding) {
     			if(!$container.attr("oht")) {
     				$container.attr("oht", $container.height())
        			$container.css("overflow", "hidden");
        			$container.animate({height:$link.parent().next().css("height")}, 150);
     			}
    		} else {
    			if($container.attr("oht")) {
    				$container.animate({height:$container.attr("oht")}, 150, function() {
        				$container.removeAttr("oht");
        				$container.removeAttr("style");
        			});
    			}
    		}
     	}
         
        function updateConsolePreviews($console) {
        	var $preview = $console.parent().next().find(".consolePreview");
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