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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose>
	<c:when test="${not node.leaf}">
		<li class='directory'>
			<span onclick="$(this).parents().first().toggleClass('directory expanded');$(this).next().toggle('fast');">${node.name}</span>
			<ul class="jqueryFileTree" style="display:none">
				<c:forEach var="node" items="${node.children}">
					<c:set var="node" value="${node}" scope="request"/>
					<jsp:include page="fileWriter.jsp"/>
				</c:forEach>
			</ul>
		</li>
	</c:when>
	<c:otherwise>
		<li class="leaf ext_${node.extension}" location="${node.path}" owner="${node.owner}"><a>${node.name}</a></li>
	</c:otherwise>
</c:choose>
