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

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h2>Moss Result: ${mossResults.date}</h2>

<p>Link: <a href="${mossResults.link}">${mossResults.link}</a> <br/>

<table id="mossTable" class="tablesorter">
	<thead>
		<tr>
			<th>Student</th>
			<th>Percentage</th>
			<th>Student</th>
			<th>Percentage</th>
			<th>Lines</th>
			<th>Max Percentage</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="pairing" items="${mossResults.pairings}">
			<tr>
				<td>${pairing.student1}</td>
				<td>${pairing.percentage1}</td>
				<td>${pairing.student2}</td>
				<td>${pairing.percentage2}</td>
				<td>${pairing.lines}</td>
				<td>${pairing.maxPercentage}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<script>
$(document).ready(function() { 
        $("#mossTable").dataTable(); 
    } 
); 
</script>