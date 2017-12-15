<%--
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
--%>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<h1>Options</h1>

<div class='section'>
	<div class='part'>
		Each option has a key and corresponding value. Do not change these values unless you are aware of the affect that changing the value will have.
	</div>
</div>

<form:form commandName="updateOptionsForm" action="updateOptions/" method="post" cssClass="">
	<div class='section'>
		<div class='part no-line'>
			<h2 class='part-title horizontal-block'>Current Options</h2>
			<input id='search' type='text' />
		</div>
		
		<div class='part'>
			<c:if test="${not empty updateOptionsForm.options}">
				<div class='pasta-form wide'>
					<table class='options-table'>
						<thead><tr><th>Key</th><th>Value</th><th></th></tr></thead>
						<tbody>
							<c:forEach var="option" items="${updateOptionsForm.options}" varStatus="s">
								<tr>
									<td>
										<form:errors element="div" path="options[${s.index}].key"/>
										<form:hidden path="options[${s.index}].key"/>
										<div class='key'><c:out value="${option.key}" /></div>
									</td>
									<td class='value'>
										<div class="pf-item compact">
											<c:choose>
											<c:when test="${fn:endsWith(option.key, '.text')}">
												<form:textarea path="options[${s.index}].value"/>
											</c:when>
											<c:otherwise>
												<form:input type="text" path="options[${s.index}].value"/>
											</c:otherwise>
											</c:choose>
										</div>
									</td>
									<td>
										<a class='delete-option'><span class='fa fa-lg fa-trash-o'></span></a>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</c:if>
			<c:if test="${empty updateOptionsForm.options}">
				<p>No options to display.
			</c:if>
			<div class="button-panel">
				<button type='submit'>Save Changes</button>
			</div>
		</div>
	</div>
	
	<div class='section'>
		<h2 class='section-title'>Add Options</h2>
		<div class='part no-line'>
			<h3 class='part-title'>Add or change a single option</h3>
			Using an existing key will result in the value for that key being changed.
		</div>
		<div class='part'>
			<div class='pasta-form'>
				<div class='pf-horizontal two-col'>
					<div class='pf-item'>
						<div class='pf-label'>Key</div>
						<div class='pf-input'>
							<form:input type="text" path="addKey"/>
							<form:errors path="addKey"/>
						</div>
					</div>
					<div class='pf-item'>
						<div class='pf-label'>Value</div>
						<div class='pf-input'>
							<form:input type="text" path="addValue"/>
							<form:errors path="addValue"/>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class='part no-line'>
			<h3 class='part-title'>Add or change multiple options</h3>
			One option per line, key/value separated by '=':
			<pre>key1=value1
<%--		 --%>key2=value2</pre>
		</div>
		<div class='part no-line'>
			<div class='pasta-form'>
				<div class='pf-item one-col'>
					<div class='pf-label'>Options</div>
					<div class='pf-input'>
						<form:errors path="addOptions"/>
						<form:textarea path="addOptions"/>
					</div>
				</div>
			</div>
		</div>
		<div class="button-panel">
			<button type='submit'>Save Changes</button>
		</div>
	</div>
</form:form>

<spring:hasBindErrors name="updateAssessmentForm">
	<script>
		$(".error:first")[0].scrollIntoView();
	</script>
</spring:hasBindErrors>

<script>
	$(".delete-option").on("click", function() {
		var row = $(this).closest("tr");
		row.toggleClass("to-delete")
		row.find(".fa").toggleClass("fa-trash-o fa-undo");
		row.find("input, textarea").prop("disabled", row.is(".to-delete"));
	});
	
	var faded = false;
	$("form").on("submit", function() {
		var toDelete = $(".to-delete");
		if(!toDelete.length) {
			return true;
		}
		toDelete.find(".delete-option").trigger("click")
		toDelete.find("input[type='hidden'][id$='.key']").val("");
		toDelete.css("background-color", "#FCC").fadeOut("fast", function() {
			if(!faded) {
				$("form").submit();
				faded = true;
			}
		});
		return false;
	});
	
	$(".options-table tbody tr").searchNode();
	$(".options-table .key, .options-table input, .options-table textarea").searchable();
	$("#search").searchBox();
</script>