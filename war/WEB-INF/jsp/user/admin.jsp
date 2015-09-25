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

<h1>PASTA Admin</h1>
<!-- IF using DBAuth-->
<c:if test="${authType == 'pasta.login.DBAuthValidator'}">
<h2>Authentication</h2>
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
<c:if test="${user.instructor}">

	<h2>Authentication System</h2>
		
	<form method="get" action="auth/" autocomplete="off">
		<table>
			<tr>
				<td>Authentication type:</td>
				<td>
					<select name="type" id="type"  path="type" onChange="addressChanged();">
						<option value="ftp" <c:if test="${authType == 'pasta.login.FTPAuthValidator'}"> selected="selected" </c:if> >FTP</option>
						<option value="imap" <c:if test="${authType == 'pasta.login.ImapAuthValidator'}"> selected="selected" </c:if> >IMAP</option>
						<option value="database" <c:if test="${authType == 'pasta.login.DBAuthValidator'}"> selected="selected" </c:if> >Database</option>
						<option value="ldap" <c:if test="${authType == 'pasta.login.LDAPAuthValidator'}"> selected="selected" </c:if> >LDAP</option>
						<option value="dummy" <c:if test="${authType == 'pasta.login.DummyAuthValidator'}"> selected="selected" </c:if> >No Authentication</option>
					</select>
				</td>
			</tr>
			<tr class='addressRow'>
				<td>Server Addresses:</td>
				<td id='addressList'>
					<c:forEach var="address" items="${addresses}">
						<div><input name="address" id="address" value="${address}" /></div>
					</c:forEach>
				</td>
			</tr>
			<tr class='addressRow'>
				<td>Add Address:</td>
				<td id='newAddress'>
					<div class='horizontal-block'><input name="address" id="address" /></div>
					<div class='horizontal-block'><button type="button" onclick="cloneRowAbove()">Add</button></div>
				</td>
			</tr>
			<tr><td colspan='2'><button type="submit" >Change Authentication System</button></td></tr>
		</table>
		
	</form>
	
	<h2>Legacy Content</h2>
	<p>Click <a href="../legacy/convert/">here</a> to go to the legacy content page.
</c:if>
<c:if test="${user.tutor}">

	<h2>Tutors</h2>
		<table class='dataTable hover row-border'>
			<thead>
				<tr><th>Username</th><th>Role</th><th>Tutorial(s)</th><c:if test="${user.instructor}"><th></th></c:if></tr>
			</thead>
			<tbody>
				<c:forEach var="person" items="${people}">
					<c:if test="${person.tutor}">
						<tr>
							<td>${person.username}</td><td>${person.permissionLevel}</td><td><c:if test="${empty person.tutorial}">-</c:if>${person.tutorial}</td>
							<c:if test="${user.instructor}">
								<td>
									<div>
										<div title='Delete' class='icon_delete'></div>
										<div title='Confirm' style='display:none' class='icon_delete_confirm' username='${person.username}'></div>
									</div>
								</td>
							</c:if>
						</tr>
					</c:if>
				</c:forEach>
			</tbody>
		</table>
		
		<c:if test="${user.instructor}">
			<button id="tutorUpdate" onclick="popup(true, false);">Update</button>
			<button id="tutorReplace" onclick="popup(true, true);">Replace</button>
		</c:if>
	
		<h2>Students</h2>
		<table class='dataTable hover row-border'>
			<thead>
				<tr><th>Username</th><th>Stream</th><th>Tutorial</th><th></th></tr>
			</thead>
			<tbody>
				<c:forEach var="person" items="${people}">
					<c:if test="${not person.tutor}">
						<tr username='${person.username}'>
							<td><a class='gc-link' href="../student/${person.username}/home/">${person.username}</a></td>
							<td>${person.stream}</td>
							<td>${person.tutorial}</td>
							<td>
								<div>
									<div title='Delete' class='icon_delete'></div>
									<div title='Confirm' style='display:none' class='icon_delete_confirm' username='${person.username}'></div>
								</div>
							</td>
						</tr>
					</c:if>
				</c:forEach>
			</tbody>
		</table>
		
		<button id="studentUpdate" onclick="popup(false, false);">Update</button>
		<button id="studentReplace" onclick="popup(false, true);">Replace</button>
		
		<div id="confirmPopup" class='popup'>
			<span class="button bClose"> <span><b>X</b></span>
			</span>
			<h1 id="updateHeading">Heading</h1>
			<div id='updateDescription'>Description</div>
			<form:form commandName="updateUsersForm" action="updateUsers/" enctype="multipart/form-data" method="post">
				<form:hidden path="updateTutors" value="false"/>
				<form:hidden path="replace" value="false"/>
				<table class='alignCellsTop'>
					<tr><td>CSV File (no headers):</td><td><form:input type='file' path="file"/></td></tr>
					<tr><td style='text-align:center;'><p><strong>OR</strong></td><td></td></tr>
					<tr>
						<td>Plain text:</td>
						<td>
							<form:errors element="div"/>
							<form:errors path="updateContents" element="div"/>
							<form:textarea path="updateContents" rows="10" cols="50"/>
						</td>
					</tr>
				</table>
				<div>
					<button type="submit">Submit</button>
				</div>
			</form:form>
		</div>
		
		<div>
			<h2>Executing Submissions</h2>
			<div class='vertical-block'>
				<c:if test="${empty taskDetails}">
					<p>None
				</c:if>
				<c:forEach items="${taskDetails}" var="detail">
					<pre>${detail}</pre>
				</c:forEach>
			</div>
			<c:if test="${user.instructor}">
				<div class='vertical-block'>
					<form action="forceSubmissionRefresh/" method="post">
						<button type="submit">Force Reload</button>
					</form>
				</div>
			</c:if>
		</div>
		
		<script>
			function popup(tutor, replace){
				var heading = "";
				heading += replace ? "Replace " : "Update ";
				heading += tutor ? "tutors " : "students ";
				
				heading += "list using csv format.";
				
				var example = tutor ?
						"username,{tutor|instructor},classes(separated by commas)" :
						"username,stream,class";
				
				var content = "<p> e.g. <br/>&nbsp;&nbsp;<code>" + example + "</code><br/>&nbsp;&nbsp;<code>" + example + "</code>";
				$('#updateDescription').html(content);
				$('#updateHeading').html(heading);
				$('#updateUsersForm #updateTutors').val(tutor);
				$('#updateUsersForm #replace').val(replace);

				$('#confirmPopup').bPopup();
			}
			
			function cloneRowAbove(){
				$("#addressList").append($('#newAddress').children().first().removeClass("horizontal-block"));
				$('#newAddress').prepend('<div class="horizontal-block"><input name="address" id="address" /></div>')
			}
			
			function addressChanged(){
				var value = document.getElementById("type").value;
				
				if(value == "ftp" || value == "imap"){
					$(".addressRow").show();
				}
				else{
					$(".addressRow").hide();
				}
				
				$('#address').val('');
			}
			
			$(function () {
			});
			
			$(function() {
				<c:if test="${authType == 'pasta.login.DBAuthValidator' or authType == 'pasta.login.DummyAuthValidator'}">
					$('.addressRow').hide();
				</c:if>
				
				$('.dataTable').dataTable({
					"columnDefs" : [ { "orderable" : false, "targets" : -1 } ]
				});
				
				<spring:hasBindErrors name='updateUsersForm'>
					popup(${updateUsersForm.updateTutors}, ${updateUsersForm.replace});
				</spring:hasBindErrors>
			});
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

	$(function() {
		$(document).on('click', "div.icon_delete", function() {
			$(this).slideToggle(50, function(){$(this).next().slideToggle('fast');});
		});
		$(document).on('mouseout', "div.icon_delete_confirm", function() {
			$(this).slideToggle(50, function(){$(this).prev().slideToggle('fast')});
		});
		$(document).on('click', "div.icon_delete_confirm", function() {
			window.location.href = 'delete/' + $(this).attr("username") + '/';
		});
	});
	
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