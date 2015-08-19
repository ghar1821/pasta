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

<h1>${competition.name}</h1>

<c:choose>
	<c:when test="${empty marks}">
		No results available
	</c:when>
	<c:otherwise>
		<table>
			<tr>
				<th>Position</th><th>Username</th><th>Percentage</th>
				<c:choose>
					<c:when test="${not user.tutor}">
						<c:forEach var="category" items="${arenaResult.studentVisibleCategories}">
							<th>${category}</th>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<c:forEach var="category" items="${arenaResult.categories}">
							<th>${category}</th>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</tr>
			<c:forEach var="positionData" items="${marks.positions}" varStatus="position">
				<c:forEach var="compResult" items="${positionData.userResults}">
					<tr>
						<td>
							${position.index + 1}
							<c:if test="${fn:length(positionData.userResults) > 1}">
								=
							</c:if>
						</td>
						<td>${compResult.username}</td>
						<td>${compResult.percentage}</td>
						<c:choose>
							<c:when test="${not user.tutor}">
								<c:forEach var="category" items="${arenaResult.studentVisibleCategories}">
									<td>${arenaResult.data[compResult.username][category]}</td>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<c:forEach var="category" items="${arenaResult.categories}">
									<td>${arenaResult.data[compResult.username][category]}</td>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>
