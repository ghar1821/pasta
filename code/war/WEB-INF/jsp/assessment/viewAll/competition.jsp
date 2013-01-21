<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h1>Competitions</h1>

<table class="pastaTable">
	<c:forEach var="competition" items="${allCompetitions}">
		<tr>		
			<td class="pastaTF pastaTF${competition.tested}">
				<!-- status -->
				<c:choose>
					<c:when test="${competition.tested}">
						TESTED
					</c:when>
					<c:otherwise>
						UNTESTED
					</c:otherwise>
				</c:choose>
			</td>
			<td>
				<!-- name -->
				<b>${competition.name}</b><br/>
				<c:choose>
					<c:when test="${competition.calculated}">
						Calculated Competition
					</c:when>
					<c:otherwise>
						Arena Competition <br/>
						${fn:length(competition.arenas)} arenas registered
					</c:otherwise>
				</c:choose>
			</td>
			<td>
				<!-- buttons -->
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="location.href='./${competition.shortName}/'">Details</button>
				</div>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="location.href='./view/${competition.shortName}/'">Competition Page</button>
				</div>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
					<button style="float:left; display:none; text-align: center; " onclick="location.href='./delete/${competition.shortName}/'" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
				</div>
			</td>
		</tr>
	</c:forEach>
</table>

<button id="newPopup">Add a new Competition</button>

<div id="newCompetition" >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> New Competition </h1>
	<form:form commandName="newCompetitionModel" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Competition Name:</td><td><form:input autocomplete="off" type="text" path="testName" value=""/></td></tr>
			<tr><td>Competition Type:</td>
				<td>
					<form:select path="type">
						<option value="calculated">Calculated</option>
						<option value="arena">Arena</option>
					</form:select>
				</td></tr>
			<tr><td>Competition Code:</td><td><form:input type="file" path="file"/></td></tr>
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
                $('#newCompetition').bPopup();

            });
            
        });

    })(jQuery);
</script>
