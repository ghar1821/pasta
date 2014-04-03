<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>Hand Marking Templates</h1>

<table class="pastaTable">
	<c:forEach var="handMarking" items="${allHandMarking}">
		<tr>
			<td><b>${handMarking.name}</b></td>
			<td>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="location.href='./${handMarking.shortName}/'">Details</button>
				</div>
				<div style="float:left">
					<button style="float:left; text-align: center; " onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
					<button style="float:left; display:none; text-align: center; " onclick="location.href='./delete/${handMarking.shortName}/'" onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
				</div>
			</td>
		</tr>
	</c:forEach>
</table>

<button id="newPopup">Add a new Hand marking template</button>

<div id="comfirmPopup" >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<button id="comfirmDeleteButton" onClick="">Confirm Deletion</button>
</div>

<div id="newHandMarking" >
	<span class="button bClose">
		<span><b>X</b></span>
	</span>
	<h1> New Hand Marking </h1>
	<form:form commandName="newHandMarkingModel" enctype="multipart/form-data" method="POST">
		<table>
			<tr><td>Hand Marking template name:</td><td><form:input autocomplete="off" type="text" path="name" value=""/></td></tr>
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
                $('#newHandMarking').bPopup();

            });
            
        });

    })(jQuery);
</script>