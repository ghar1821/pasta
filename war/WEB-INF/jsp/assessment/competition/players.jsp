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

<h1>Competition: ${competition.name}</h1>
<c:choose>
	<c:when test="${empty viewedUser}">					
		<button id="newPopup">Add a new Player</button>
	</c:when>
	<c:otherwise>
		<h1>Username: ${viewedUser.username}</h1>
	</c:otherwise>
</c:choose>

<spring:hasBindErrors name="newPlayerModel">
	<form:form commandName="newPlayerModel" enctype="multipart/form-data" method="POST">
		<h3>Submission Errors</h3>
		<form:errors path="file" cssClass="ui-state-error" element="pre" />
	</form:form>
</spring:hasBindErrors>

<h2>Active Players:</h2>
<table>
	<c:forEach var="player" items="${players}">
		<c:if test="${not empty player.activePlayer}">
			<tr>
				<td><h5>${player.activePlayer.name}</h5></td><td><h5>Uploaded: ${player.activePlayer.firstUploaded}</h5></td>
			</tr>
			<c:if test="${user.tutor}">
				<tr>
					<td>
						<c:set var="node" value="${nodeList[player.activePlayer.name]}" scope="request"/>
						<jsp:include page="../../recursive/fileWriterRoot.jsp">
							<jsp:param name="owner" value="${viewedUser.username}"/>
						</jsp:include>
					</td>
				</tr>
			</c:if>
			<tr>
				<td>
					<div style="position: absolute; float:left; z-index:2; margin:145px 105px"><strong>#${player.activePlayer.officialRanking}</strong></div>
					<div id="officialdonut${player.activePlayer.name}" style="width: 300px; height: 300px;"></div>
				</td>
				<td>
					<div id="unofficialdonut${player.activePlayer.name}" style="width: 300px; height: 300px;"></div>
					<script type="text/javascript">
					  google.load("visualization", "1", {packages:["corechart"]});
					  google.setOnLoadCallback(drawChart);
					  function drawChart() {
						var officialData = google.visualization.arrayToDataTable([
						  ['Outcome', 'Numbers'],
						  ['Win', ${player.activePlayer.officialWin}],
						  ['Loss', ${player.activePlayer.officialLoss}],
						  ['Draw', ${player.activePlayer.officialDraw}]
						]);
						
						var unofficialData = google.visualization.arrayToDataTable([
						  ['Outcome', 'Numbers'],
						  ['Win', ${player.activePlayer.unofficialWin}],
						  ['Loss', ${player.activePlayer.unofficialLoss}],
						  ['Draw', ${player.activePlayer.unofficialDraw}]
						]);

						var officialOptions = {
						  title: 'Official Stats',
					      pieSliceText: 'none',
						  pieHole: 0.4,
						};
						var unofficialOptions = {
						  title: 'Unofficial Stats',
						  pieSliceText: 'none',
						  pieHole: 0.4,
						};

						var officialChart = new google.visualization.PieChart(document.getElementById('officialdonut${player.activePlayer.name}'));
						officialChart.draw(officialData, officialOptions);
						var unofficialChart = new google.visualization.PieChart(document.getElementById('unofficialdonut${player.activePlayer.name}'));
						unofficialChart.draw(unofficialData, unofficialOptions);
					  }
				</script>
				</td>
				
			</tr>
			<c:if test="${empty viewedUser}">					
				<tr>
					<td>
						<button onclick="location.href='retire/${player.activePlayer.name}/'">RETIRE</button>
					</td>
				</tr>
			</c:if>
		</c:if>
	</c:forEach>
</table>

<h2>Retired Players:</h2>
<table>
	<c:forEach var="player" items="${players}">
		<c:forEach var="oldPlayer" items="${player.retiredPlayers}">
			<tr><td>${oldPlayer.name} - ${oldPlayer.firstUploaded} W: ${oldPlayer.officialWin} D: ${oldPlayer.officialDraw} L: ${oldPlayer.officialLoss}</td></tr>
		</c:forEach>
	</c:forEach>
</table>

<c:if test="${empty viewedUser}">					

	<div id="newPlayer" class="popup">
		<span class="button bClose">
			<span><b>X</b></span>
		</span>
		<h1> New Player </h1>
		<form:form commandName="newPlayerModel" enctype="multipart/form-data" method="POST">
			<table>
				<tr><td>Player Code:</td><td><form:input type="file" path="file"/></td></tr>
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
	                $('.popup').bPopup();
	
	            });
	            
	        });
	
	    })(jQuery);
	</script>
</c:if>