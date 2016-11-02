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

<div class='float-container'>
	<div class='horizontal-block'>
		<h1>Group Management - ${assessment.name}</h1>
	</div>
	<input id='search' type='text' />
</div>

<div class='section'>
	<h2 class='section-title'>My Group</h2>
	<c:if test="${empty myGroup}">
		<div class='part'>You are not in a group.</div>
	</c:if>
	<c:if test="${not empty myGroup}">
		<div class='part row'>
			<div class='horizontal-block'>
				<h4 class='part-title'>
					<span class='icon_locked lockToggle' group='${myGroup.id}' ${myGroup.locked ? "" : "style='display:none'"}></span>
					Group ${myGroup.number}
				</h4>
				<c:forEach var="member" items="${myGroup.members}">
					<span class='group-member'>${member.username}</span>
				</c:forEach>
			</div>
			<c:if test="${user.tutor || (assessment.studentsManageGroups && !assessment.groupsLocked)}">
				<div class='button-panel'>
					<button type='button' class='flat async do-form lockToggle' data-action="unlockGroup/${myGroup.id}/" group='${myGroup.id}' ${myGroup.locked ? "" : "style='display:none'"}>Unlock Group</button>
					<button type='button' class='flat async do-form lockToggle' data-action="lockGroup/${myGroup.id}/" group='${myGroup.id}' ${myGroup.locked ? "style='display:none'" : ""}>Lock Group</button>
					<button type='button' class='flat do-form leaveForm' data-action="leaveGroup/">Leave Group</button>
				</div>
			</c:if>
		</div>
	</c:if>
</div>

<div class='section'>
	<h2 class='section-title'>Other Existing Groups</h2>
	<c:if test="${empty otherGroups}">
		<div class='part'>No other groups.</div>
	</c:if>
	<c:forEach var="group" items="${otherGroups}">
		<div class='part row'>
			<div class='horizontal-block'>
				<h4 class='part-title'>
					<span class='icon_locked lockToggle' group='${group.id}' ${group.locked ? "" : "style='display:none'"}></span>
					Group ${group.number}
				</h4>
				<c:choose>
					<c:when test="${empty group.members}">
						<span class='no-members'>No members</span>
					</c:when>
					<c:otherwise>
						<c:forEach var="member" items="${group.members}">
							<span class='group-member'>${member.username}</span>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</div>
			<c:if test="${user.tutor || assessment.studentsManageGroups}">
				<div class='button-panel'>
					<c:if test="${user.tutor}">
						<button type='button' class='flat do-form async lockToggle' data-action="unlockGroup/${group.id}/" group='${group.id}' ${group.locked ? "" : "style='display:none'"}>Unlock Group</button>
						<button type='button' class='flat do-form async lockToggle' data-action="lockGroup/${group.id}/" group='${group.id}' ${group.locked ? "style='display:none'" : ""}>Lock Group</button>
					</c:if>
					<c:choose>
						<c:when test="${group.full}">
							<button type='button' class='flat' disabled='disabled'>Group Full</button>
						</c:when>
						<c:otherwise>
							<c:if test="${user.tutor || (!assessment.groupsLocked && !group.locked)}">
								<button type='button' class='flat joinForm do-form' data-action="joinGroup/${group.id}/">Join Group</button>
							</c:if>
						</c:otherwise>
					</c:choose>
				</div>
			</c:if>
		</div>
	</c:forEach>
</div>

<c:if test="${assessment.unlimitedGroupCount && (user.tutor || (assessment.studentsManageGroups && !assessment.groupsLocked && empty myGroup))}">
	<button class='floating plus do-form' data-action="addGroup/" title='Add a new group'></button>
</c:if>

<c:if test="${user.tutor}">
	<div class='section'>
		<h2 class='section-title'>Manage Students</h2>
		<div class='part'>
			Edit the "Group" column to change the group of a student. Select and edit multiple using <kbd>Shift</kbd> or <kbd>Ctrl</kbd>.
		</div>
		<div class='button-panel'>
			<form:form commandName="updateGroupsForm" action="saveAll/" method="post">
				<button id='saveAllGroups'>Save Changes</button>
			</form:form>
		</div>
		<div class='part'>
			<table id='groupSelection'>
				<thead><tr><td>Username</td><td>Stream</td><td>Tutorial</td><td>Group</td><td>Group Size</td></tr></thead>
				<tbody>
					<c:forEach var="student" items="${allStudents}">
						<tr>
							<td>${student.username}</td>
							<td>${student.stream}</td>
							<td>${student.tutorial}</td>
							<td class='group-set pasta-form no-width' >
								<div class='pf-item compact'><input class='group-input' type='text' value='${studentGroups[student.username]}'/></div>
							</td>
							<td class='group-size'></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
		<div class='button-panel'>
			<button type='button' onclick='$("#saveAllGroups").click()'>Save Changes</button>
		</div>
	</div>
</c:if>

<script>
	$(function() {
		$('.leaveForm<c:if test="${not empty myGroup}">,.joinForm</c:if>').on("click", function(e) {
			var confirmResult = confirm("Are you sure you want to leave your current group?");
			if(!confirmResult) {
				e.preventDefault();
				return false;
			}
		});
		
		$.fn.dataTable.ext.order['dom-text'] = function(settings, col) {
		    return this.api().column(col, {order:'index'}).nodes().map(function(td, i) {
		        return $('input', td).val();
		    });
		};
		var groupTable = $("#groupSelection").DataTable({
			columns: [{ name: 'username' },{},{},{ "orderDataType": "dom-text" }, {}],
			"paging" : false,
			"info" : false,
			"language": {
			    "emptyTable": "No students without a group."
			 }
		});

		var groupMap = {<c:forEach var="group" items="${allGroups}">${group.number}:${group.id},</c:forEach>};
		
		var lastClick = null;
		$("#groupSelection").on('click', 'tr', function (e) {
			if($(e.target).is(".group-set") || $(e.target).is(".group-input")) {
				return false;
			}
			
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
		
		$(".group-input").on("focusout", function(e) {
			$("#groupSelection .group-input").removeClass("editing");
		});
		$(".group-input").on("focusin", function(e) {
			if($(this).closest("tr").is(".selected")) {
				$("#groupSelection tr.selected .group-input").addClass("editing");
			} else {
				$(this).addClass("editing");
			}
			e.preventDefault();
		});
		var checkTimer;
		$(".group-input").on("keyup change", function() {
			if($(this).closest("tr").is(".selected")) {
				$("#groupSelection tr.selected .group-input").val($(this).val());
			}
			clearTimeout(checkTimer);
			checkTimer = setTimeout(checkGroupSizes, 800);
		});
		
		// Thanks to Pebbl at http://stackoverflow.com/questions/5060652/jquery-selector-for-input-value
		jQuery.extend(jQuery.expr[':'], {
			/// check that a field's value property has a particular value
			'field-value': function(el, indx, args) {
				var a, v = $(el).val();
				if ((a = args[3])) {
					switch (a.charAt(0)) {
					/// begins with
					case '^':
						return v.substring(0, a.length - 1) == a.substring(1, a.length);
						break;
					/// ends with
					case '$':
						return v.substr(v.length - a.length - 1, v.length) == a.substring(1, a.length);
						break;
					/// contains
					case '*':
						return v.indexOf(a.substring(1, a.length)) != -1;
						break;
					/// equals
					case '=':
						return v == a.substring(1, a.length);
						break;
					/// not equals
					case '!':
						return v != a.substring(1, a.length);
						break;
					/// equals
					default:
						return v == a;
						break;
					}
				} else {
					return !!v;
				}
			}
		});
		
		function checkGroupSizes() {
			var limit = ${assessment.groupSize};
			var allGood = true;
			$("#groupSelection tr").each(function(i, row) {
				var group = +$(row).find(".group-input").val();
				var valid = group > 0 && groupMap[group] !== undefined;

				var $cell = $(row).children(".group-size");
				if(!$cell.length){
					return true;
				}
				var size = $("#groupSelection .group-input:field-value(" + group + ")").length;
				
				$cell.toggleClass("overFull", valid && limit >= 0 && size > limit);
				$cell.toggleClass("full", valid && limit >= 0 && size == limit);
				
				var info = "";
				var extra = "";
				if(valid) {
					info = size + (limit >= 0 ? " of " + limit : "");
				}
				if(valid && limit >= 0 && size >= limit) {
					extra = "Full"
				}
				if(valid && limit >= 0 && size > limit) {
					extra += " +" + (size - limit).toString();
					allGood = false;
				}
				
				if(info) {
					groupTable.cell($cell[0]).data(info + (extra ? " (" + extra + ")" : ""));
				} else {
					groupTable.cell($cell[0]).data("");
				}
			});
			return allGood;
		}
		checkGroupSizes();
		
		$('#saveAllGroups').on('click', function(e) {
			var tablesOkay = checkGroupSizes();
			if(!tablesOkay) {
				var check = confirm("You are allowing groups to be over-full. Is this okay?");
				if(!check) {
					e.preventDefault();
					return false;
				}
			}
			var $form = $('#updateGroupsForm');
			var groupIndices = {};
			$("#groupSelection tr").each(function() {
				var groupNum = $(this).find(".group-input").val();
				var groupId = groupMap[groupNum];
				if(groupId !== undefined) {
					var username = groupTable.cell(this, "username:name").data();
					var index = groupIndices[groupId];
					if(index === undefined) {
						index = 0;
					}
					var input = $("<input/>", {
						type: 'hidden',
						name: 'groupMembers[' + groupId + '][' + index + ']',
						value: username
					});
					groupIndices[groupId] = index + 1;
					$form.append(input);
				}
			});
		});
		
		$(".do-form").on("click", function(e) {
			if (window.event ? event.defaultPrevented : e.isDefaultPrevented()){
				return false;
		    }
			var $source = $(this);
			var action = $source.data("action");
			if($source.is(".async")) {
				var group = $source.attr("group");
				asyncForm(action, 
					function(data) {
						if(!data) {
							$(".lockToggle[group='" + group + "']").toggle();
						}
					}, function() {
						alert("Failed to change lock. Please try again later.");
					});
			} else {
				syncForm(action);
			}
		});
		function asyncForm(action, success, failure) {
			$.ajax({
				headers : {
					'Accept' : 'application/json',
				},
				url : action,
				type : "POST",
				statusCode : {
					500 : failure
				},
				success : success
			});
		}
		function syncForm(action) {
			$("<form/>")
				.attr("action", action)
				.attr("method", "POST")
				.submit()
		}
		
		$(".part.row").searchNode();
		$(".group-member,.part-title").searchable();
		var searchBox = $("#search").searchBox();
	});
</script>