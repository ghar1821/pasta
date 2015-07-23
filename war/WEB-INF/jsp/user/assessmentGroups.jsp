<!-- 
Copyright (c) 2015, Josh Stretton
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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h1>Group Management - ${assessment.name}</h1>

<h2>My Group</h2>
<c:if test="${empty myGroup}">
	<p>You are not in a group.
</c:if>
<c:if test="${not empty myGroup}">
	<div class='boxCard vertical-block float-container'>
		<div class='horizontal-block'>
			<h4 class='compact'>
				<span class='icon_locked lockToggle' group='${myGroup.id}' ${myGroup.locked ? "" : "style='display:none'"}></span>
				Group ${myGroup.number}
			</h4>
			<p><strong>Current members:</strong>
			<ul>
				<c:forEach var="member" items="${myGroup.members}">
					<li>${member.username}
				</c:forEach>
			</ul>
		</div>
		<c:if test="${!user.tutor && assessment.studentsManageGroups && !assessment.groupsLocked}">
			<div class='horizontal-block float-right'>
				<form class='leaveForm' action="leaveGroup/" method="post">
					<button>Leave Group</button>
				</form>
			</div>
			<div class='horizontal-block float-right lockToggle' group='${myGroup.id}' ${myGroup.locked ? "" : "style='display:none'"}>
				<form class='lockForm async' action="unlockGroup/${myGroup.id}/" method="post">
					<button>Unlock Group</button>
				</form>
			</div>
			<div class='horizontal-block float-right lockToggle' group='${myGroup.id}' ${myGroup.locked ? "style='display:none'" : ""}>
				<form class='lockForm async' action="lockGroup/${myGroup.id}/" method="post">
					<button>Lock Group</button>
				</form>
			</div>
		</c:if>
	</div>
</c:if>

<h2>Other Existing Groups</h2>
<c:if test="${empty otherGroups}">
	<p>No other groups.
</c:if>
<c:forEach var="group" items="${otherGroups}">
	<div class='boxCard vertical-block float-container'>
		<div class='horizontal-block'>
			<h4 class='compact'>
				<span class='icon_locked lockToggle' group='${group.id}' ${group.locked ? "" : "style='display:none'"}></span>
				Group ${group.number}
			</h4>
			<p><strong>Current members:</strong>
			<c:choose>
				<c:when test="${empty group.members}">
					<p>No members.
				</c:when>
				<c:otherwise>
					<ul>
						<c:forEach var="member" items="${group.members}">
							<li>${member.username}
						</c:forEach>
					</ul>
				</c:otherwise>
			</c:choose>
		</div>
		<c:if test="${user.tutor || assessment.studentsManageGroups}">
			<div class='horizontal-block float-right'>
				<c:choose>
					<c:when test="${group.full}">
						<h2 class='compact'>Group Full</h2>
					</c:when>
					<c:otherwise>
						<c:if test="${!user.tutor && !assessment.groupsLocked && !group.locked}">
							<form class='joinForm' action="joinGroup/${group.id}/" method="post">
								<button>Join Group</button>
							</form>
						</c:if>
					</c:otherwise>
				</c:choose>
			</div>
			<c:if test="${user.tutor}">
				<div class='horizontal-block float-right lockToggle' group='${group.id}' ${group.locked ? "" : "style='display:none'"}>
					<form class='lockForm async' action="unlockGroup/${group.id}/" method="post">
						<button>Unlock Group</button>
					</form>
				</div>
				<div class='horizontal-block float-right lockToggle' group='${group.id}' ${group.locked ? "style='display:none'" : ""}>
					<form class='lockForm async' action="lockGroup/${group.id}/" method="post">
						<button>Lock Group</button>
					</form>
				</div>
			</c:if>
		</c:if>
	</div>
</c:forEach>

<c:if test="${assessment.unlimitedGroupCount && (user.tutor || (assessment.studentsManageGroups && !assessment.groupsLocked && empty myGroup))}">
	<div class='vertical-block'>
		<form class='addGroupForm' action="addGroup/" method="post">
			<button>Add a New Group</button>
		</form>
	</div>
</c:if>

<c:if test="${user.tutor}">
	<h2>Manage Students</h2>
	<p> Drag students from the "Students with no group" section to other groups. Select multiple using <kbd>Shift</kbd> or <kbd>Ctrl</kbd>.
	<div class='vertical-block'>
		<form:form commandName="updateGroupsForm" action="saveAll/" method="post">
			<button id='saveAllGroups'>Save Changes</button>
		</form:form>
	</div>
	<div class='vertical-block'>
		<div class='horizontal-block boxCard group allStudents'>
			<h4 class='compact' style='margin-bottom:1em;'>Students with no group</h4>
			<table class='groupSelection'>
				<thead><tr><td>Username</td><td>Stream</td><td>Tutorial</td></tr></thead>
				<tbody>
					<c:forEach var="student" items="${allStudents}">
						<tr><td>${student.username}</td><td>${student.stream}</td><td>${student.tutorial}</td></tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
		<div class='group separator'>
			&#8644;
		</div>
		<div class='horizontal-block float-container group allGroups'>
			<c:forEach var="group" items="${allGroups}">
				<div class='float-left boxCard' groupId='${group.id}'>
					<h4 class='compact'>Group ${group.number} <span id='fullInfo' class='float-right'></span></h4>
					<table class='groupSelection'>
						<thead><tr><td>Username</td><td>Stream</td><td>Tutorial</td></tr></thead>
						<tbody>
							<c:forEach var="student" items="${group.members}">
								<tr><td>${student.username}</td><td>${student.stream}</td><td>${student.tutorial}</td></tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</c:forEach>
		</div>
	</div>
</c:if>

<script>
	$(function() {
		$('.leaveForm<c:if test="${not empty myGroup}">,.joinForm</c:if>').on("submit", function() {
			var confirmResult = confirm("Are you sure you want to leave your current group?");
			if(!confirmResult) {
				return false;
			}
		});
		
		$("div:not(.allStudents)>table.groupSelection").DataTable({
			columns: [{ name: 'username' },{},{}],
			"paging" : false,
			"info" : false,
			"searching" : false,
			"language": {
			    "emptyTable": "No students in group."
			}
		});
		$("div.allStudents>table.groupSelection").DataTable({
			columns: [{ name: 'username' },{},{}],
			"paging" : false,
			"info" : false,
			"language": {
			    "emptyTable": "No students without a group."
			 }
		});
		
		var lastClick = null;
		$("table.groupSelection").on('click', 'tr', function (e) {
			// Only allow shift-click across same-parent rows
			if(lastClick != null && lastClick.parent()[0] != $(this).parent()[0]) {
				lastClick = null;
			}
			
			if (e.ctrlKey || e.metaKey) {
		        $(this).toggleClass("selected");
		    } else if (lastClick != null && e.shiftKey) {
		    	var findMe = this;
		    	// check direction of shift-click
		    	if(lastClick.nextAll().filter(function() {return this == findMe}).length) {
		    		lastClick.nextUntil($(this)).add($(this)).toggleClass("selected", true);
		    	} else {
		    		lastClick.prevUntil($(this)).add($(this)).toggleClass("selected", true);
		    	}
		    } else {
		        $(this).addClass("selected").siblings().removeClass('selected');
		    }
			
			lastClick = $(this).is(".selected") ? $(this) : null;
		});
		$("table.groupSelection tbody").sortable({
		    connectWith: "tbody",
		    delay: 150,
		    revert: 0,
		    helper: function (e, item) {
		        if (!item.hasClass('selected')) {
		            item.addClass('selected').siblings().removeClass('selected');
		        }
		        var elements = item.parent().children('.selected').clone();
		        item.data('multidrag', elements).siblings('.selected').remove();
		        var helper = $('<tbody/>');
		        return helper.append(elements);
		    },
		    stop: function (e, ui) {
		        var elements = ui.item.data('multidrag');
		        
		        var source = $(e.target).parent();
		        var target = ui.item.parent().parent();
		        
		        var srcTable = source.DataTable();
		        var targetTable = target.DataTable();
		        if(source.attr('id') == target.attr('id')) {
		        	srcTable.draw();
		        	return;
		        }
				
		        elements.each(function () {
		        	targetTable.row.add($(this));
		        });
		        targetTable.draw();
		        srcTable.rows(".selected").remove().draw();
		        checkTableLengths()
		    }
		});
		//$("div.dataTables_wrapper").css('min-height', '300px');
		
		checkTableLengths();
		function checkTableLengths() {
			var limit = ${assessment.groupSize};
			var allGood = true;
			$("div.group.allGroups>div").each(function() {
				var size = $(this).find("table tbody tr").length;
				$(this).toggleClass("overFull", size > limit);
				$(this).toggleClass("full", size == limit);
				var info = "";
				if(size >= limit) {
					info = "Full"
				}
				if(size > limit) {
					info += " +" + (size - limit).toString();
					allGood = false;
				}
				if(info) {
					$(this).find('#fullInfo').html("(" + info + ")");
				} else {
					$(this).find('#fullInfo').empty();
				}
			});
			return allGood;
		}
		
		$('#saveAllGroups').on('click', function(e) {
			var tablesOkay = checkTableLengths();
			if(!tablesOkay) {
				var check = confirm("You are allowing groups to be over-full. Is this okay?");
				if(!check) {
					e.preventDefault();
					return false;
				}
			}
			var $form = $('#updateGroupsForm');
			$("div.allGroups>div").each(function() {
				var groupId = $(this).attr("groupId");
				var data = $(this).find("table").DataTable().column("username:name").data();
				data.each(function(item, index) {
					var input = $("<input/>", {
						type: 'hidden',
						name: 'groupMembers[' + groupId + '][' + index + ']',
						value: item
					});
					$form.append(input);
				});
			});
		});
		
		$("form.lockForm.async").on("submit", function() {
			var $form = $(this);
			var group = $(this).parent().attr("group");
			$.ajax({
				headers : {
					'Accept' : 'application/json',
				},
				url : $form.attr("action"),
				data : $form.serialize(),
				type : "POST",
				statusCode : {
					500 : function() {
						alert("Failed to change lock. Please try again later.");
					}
				},
				success : function(data) {
					if(!data) {
						$(".lockToggle[group='" + group + "']").toggle();
					}
				}
			});
			return false;
		});
	});
</script>