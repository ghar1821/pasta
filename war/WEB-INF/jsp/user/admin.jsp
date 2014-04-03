<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!-- IF using DBAuth-->
<c:if test="${authType == 'pasta.login.DBAuthValidator'}">
<h1>Authentication</h1>
	<h2>Change Password</h2>
	<form:form method="post" commandName="changePasswordForm" action="changePassword/" autocomplete="off">
		<table>
			<tr>
				<td><form:label path="oldPassword" cssClass="required">Old Password <span class="star-required">*</span></form:label></td> 
				<td><form:password path="oldPassword" size="50" name="oldPassword" id="oldPassword" />
				<form:errors path="oldPassword" cssClass="susk-form-errors" element="div" /></td>
			</tr>
			<tr>
				<td><form:label path="newPassword" cssClass="required">New Password<span class="star-required">*</span></form:label></td> 
				<td><form:password path="newPassword" size="50" name="newPassword" id="newPassword" onkeyup="checkPasswords();"/>
				<form:errors path="newPassword" cssClass="susk-form-errors" element="div" /></td>
			</tr>
			<tr>
				<td><form:label path="confirmPassword" cssClass="required">Confirm Password <span class="star-required">*</span></form:label></td> 
				<td><form:password path="confirmPassword" size="50" name="confirmPassword" id="confirmPassword" onkeyup="checkPasswords();"/>
				<form:errors path="confirmPassword" cssClass="susk-form-errors" element="div" /></td>
			</tr>
		</table>
		
		<div>
			<button type="submit" style= "margin-left: 17.5em; padding-left: 1em;padding-right: 1em;"id="Submit" name="Submit">Change Password</button>
		</div>
		<div class="susk-form-clear"></div>
		
	</form:form>
	<div class="susk-form-clear"></div>
</c:if>

<!-- FOR INSTRUCTORS ONLY!! -->
<c:if test="${unikey.instructor}">

	<h2>Authentication System</h2>
		
	<form method="get" action="auth/" autocomplete="off">
		<select name="type" id="type"  path="type" onChange="addressChanged();">
			<option value="ftp" <c:if test="${authType == 'pasta.login.FTPAuthValidator'}"> selected="selected" </c:if> >FTP</option>
			<option value="imap" <c:if test="${authType == 'pasta.login.ImapAuthValidator'}"> selected="selected" </c:if> >IMAP</option>
			<option value="database" <c:if test="${authType == 'pasta.login.DBAuthValidator'}"> selected="selected" </c:if> >Database</option>
			<option value="ldap" <c:if test="${authType == 'pasta.login.LDAPAuthValidator'}"> selected="selected" </c:if> >LDAP</option>
			<option value="dummy" <c:if test="${authType == 'pasta.login.DummyAuthValidator'}"> selected="selected" </c:if> >No Authentication</option>
		</select>
		<div id="addressDiv" <c:if test="${authType == 'pasta.login.DBAuthValidator' or authType == 'pasta.login.DummyAuthValidator'}">style="display:none;"</c:if>>
			<table id="addressTable">
				<c:choose>
					<c:when test="${not empty addresses}">
						<c:forEach var="address" items="${addresses}">
							<tr><td><input path="address" name="address" id="address" value="${address}" /></td></tr>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<tr><td><input path="address" name="address" id="address" /></td></tr>
					</c:otherwise>
				</c:choose>
			</table>
			<button type="button" onClick="cloneRowAbove()">Add new Row</button><br/>
		</div>
		<button type="submit" >Change Authentication System</button>
	</form>
	
</c:if>
<c:if test="${unikey.tutor}">

	<h1>Tutors</h1>
		<table>
			<tr><th>Username</th><th>Role</th><th>Tutorial(s)</th></tr>
			<c:forEach var="person" items="${people}">
				<c:if test="${person.tutor}">
					<tr>
						<td>${person.username}</td><td>${person.permissionLevel}</td><td><c:if test="${empty person.tutorial}">-</c:if>${person.tutorial}</td>
						<td>
							<div style="float: left">
								<button style="float: left; text-align: center;"
									onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
								<button style="float: left; display: none; text-align: center;"
									onclick="location.href='delete/${person.username}/'"
									onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
							</div>
						</td>
					</tr>
				</c:if>
			</c:forEach>
		</table>
		
		<button id="tutorUpdate" onClick="popup(true, false);">Update</button>
		<button id="tutorReplace" onClick="popup(true, true);">Replace</button>
		
	
	<h1>Students</h1>
		<table>
			<tr><th>Username</th><th>Stream</th><th>Tutorial</th></tr>
			<c:forEach var="person" items="${people}">
				<c:if test="${not person.tutor}">
					<tr>
						<td>${person.username}</td><td>${person.stream}</td><td>${person.tutorial}</td>
						<td>
							<div style="float: left">
								<button style="float: left; text-align: center;"
									onclick="$(this).slideToggle('fast').next().slideToggle('fast')">Delete</button>
								<button style="float: left; display: none; text-align: center;"
									onclick="location.href='delete/${person.username}/'"
									onmouseout="$(this).slideToggle('fast').prev().slideToggle('fast');">Confirm</button>
							</div>
						</td>
					</tr>
				</c:if>
			</c:forEach>
		</table>
		
		<button id="studentUpdate" onClick="popup(false, false);">Update</button>
		<button id="studentReplace" onClick="popup(false, true);">Replace</button>
		
		<div id="comfirmPopup">
			<span class="button bClose"> <span><b>X</b></span>
			</span>
			<h1 id="popupText">Are you sure you want to do that?</h1>
			<form id="classlist" method="post" action="replaceStudents/">
				<textarea name="list" rows="10" cols="50"></textarea><br/>
				<button type="submit">Submit</button>
			</form>
		</div>
		
		<script>
			function popup(tutor, replace){
				
				var content = "";
				var example = "";
				var action = "";
				
				if(replace){
					content+="Replace ";
					action+="replace";
				}
				else{
					content+="Update ";
					action+="update";
				}
				
				if(tutor){
					content+="tutor ";
					example="username,role,classes(separated by commas)";
					action+="Tutors/";
				}
				else{
					content+="student ";
					example="username,stream,class";
					action+="Students/";
				}
				
				content+="list using csv format. <br/> e.g." + example;
							
				document.getElementById('classlist').action = action;
				document.getElementById('popupText').innerHTML = content;

				// Triggering bPopup when click event is fired
				$('#comfirmPopup').bPopup();
			}
			
			function cloneRowAbove(){
				$("#addressTable").append('<tr><td><input path="address" name="address" id="address" /></td></tr>');
			}
			
			function addressChanged(){
				var value = document.getElementById("type").value;
				
				if(value == "ftp" || value == "imap"){
					$("#addressDiv").show();
				}
				else{
					$("#addressDiv").hide();
				}
				
				var inputs = document.getElementsByName('address');
				for (var i = 0; i < inputs.length; i += 1) {
					inputs[i].value = '';
				}
			}
		</script>
		
</c:if>

<style>

	.glowingRed {
		outline: none;
	    border-color: #f00;
		box-shadow: 0 0 10px #f00;
	}
	
	.glowingGreen {
		outline: none;
	    border-color: #0f0;
		box-shadow: 0 0 10px #0f0;
	}

</style>

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
                $('#newArena').bPopup();

            });
            
        });

    })(jQuery);
	
	function checkPasswords(){
		if (document.getElementById('newPassword').value == document.getElementById('confirmPassword').value){
			document.getElementById("Submit").disabled = false; 
			$("#newPassword").addClass( "glowingGreen" );
			$("#newPassword").removeClass( "glowingRed" );
			$("#confirmPassword").addClass( "glowingGreen" );
			$("#confirmPassword").removeClass( "glowingRed" );
		}
		else{
			document.getElementById("Submit").disabled = true; 
			$("#newPassword").addClass( "glowingRed" );
			$("#newPassword").removeClass( "glowingGreen" );
			$("#confirmPassword").addClass( "glowingRed" );
			$("#confirmPassword").removeClass( "glowingGreen" );
		}
	}
</script>