<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>Competition: ${competition.name}</h1>
<button id="newPopup">Add a new Player</button>

<h2>Active Players:</h2>
<table>
	<c:forEach var="player" items="${players}">
		<c:if test="${not empty player.activePlayer}">
			<tr>
				<td><h5>${player.activePlayer.name}</h5></td><td><h5>Uploaded: ${player.activePlayer.firstUploaded}</h5></td>
			</tr>
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
			<tr>
				<td>
					<button onclick="location.href='retire/${player.activePlayer.name}/'">RETIRE</button>
				</td>
			</tr>
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