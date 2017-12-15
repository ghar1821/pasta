/*
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
*/

package pasta.web.controller;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.UpdateGroupsForm;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
import pasta.service.UserManager;
import pasta.web.WebUtils;

/**
 * Controller class for group functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/groups/...
 * 
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-13
 *
 */
@Controller
@RequestMapping("groups/")
public class GroupController {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserManager userManager;
	@Autowired
	private GroupManager groupManager;
	@Autowired
	private AssessmentManager assessmentManager;

	// ///////////////////////////////////////////////////////////////////////////
	// Model //
	// ///////////////////////////////////////////////////////////////////////////
	
	@ModelAttribute("updateGroupsForm")
	public UpdateGroupsForm loadUpdateGroupsForm() {
		return new UpdateGroupsForm();
	}

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// Groups //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "/{assessmentId}/", method = RequestMethod.GET)
	public String viewAssessmentGroup(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			Model model) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:/home/";
		}
		
		model.addAttribute("assessment", assessment);
		
		PASTAGroup myGroup = groupManager.getGroup(user, assessment);
		model.addAttribute("myGroup", myGroup);
		
		Set<PASTAGroup> allGroups = groupManager.getGroups(assessment);
		model.addAttribute("allGroups", allGroups);
		
		Set<PASTAGroup> otherGroups = new TreeSet<PASTAGroup>(allGroups);
		if(myGroup != null) {
			otherGroups.remove(myGroup);
		}
		model.addAttribute("otherGroups", otherGroups);
		
		Set<PASTAUser> noGroupUsers = new TreeSet<PASTAUser>(userManager.getStudentList());
		Set<PASTAUser> allUsers = new TreeSet<PASTAUser>(userManager.getStudentList());
		HashMap<String, Integer> userGroups = new HashMap<>();
		for(PASTAGroup group : allGroups) {
			noGroupUsers.removeAll(group.getMembers());
			for(PASTAUser member : group.getMembers()) {
				userGroups.put(member.getUsername(), group.getNumber());
			}
		}
		model.addAttribute("noGroupStudents", noGroupUsers);
		model.addAttribute("allStudents", allUsers);
		model.addAttribute("studentGroups", userGroups);
		
		return "user/assessmentGroups";
	}
	
	@RequestMapping(value = "/{assessmentId}/leaveGroup/", method = RequestMethod.POST)
	public String leaveGroup(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			Model model) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:/home/";
		}
		
		groupManager.leaveCurrentGroup(user, assessment);
		
		return "redirect:../";
	}
	
	@RequestMapping(value = "/{assessmentId}/joinGroup/{groupId}/", method = RequestMethod.POST)
	public String joinGroup(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("groupId") long groupId,
			Model model) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:/home/";
		}
		
		PASTAGroup group = groupManager.getGroup(groupId);
		if(group == null) {
			return "redirect:../../";
		}
		
		groupManager.joinGroup(user, assessment, group);
		
		return "redirect:../../";
	}
	
	@RequestMapping(value = "/{assessmentId}/lockGroup/{groupId}/", method = RequestMethod.POST)
	@ResponseBody
	public String lockGroup(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("groupId") long groupId,
			Model model) {
		return toggleLock(user, assessmentId, groupId, model, true);
	}
	
	@RequestMapping(value = "/{assessmentId}/unlockGroup/{groupId}/", method = RequestMethod.POST)
	@ResponseBody
	public String unlockGroup(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("groupId") long groupId,
			Model model) {
		return toggleLock(user, assessmentId, groupId, model, false);
	}
	
	private String toggleLock(PASTAUser user, long assessmentId, long groupId, Model model, boolean lock) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "not released";
		}
		
		PASTAGroup group = groupManager.getGroup(groupId);
		if(group == null) {
			return "not in group";
		}
		
		groupManager.toggleLock(user, group, lock);
		
		return "";
	}
	
	@RequestMapping(value = "/{assessmentId}/addGroup/", method = RequestMethod.POST)
	public String addGroup(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			Model model) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:/home/";
		}
		
		groupManager.addGroup(assessment);
		
		return "redirect:../";
	}
	
	@RequestMapping(value = "/{assessmentId}/saveAll/", method = RequestMethod.POST)
	public String updateGroups(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			@ModelAttribute("updateGroupsForm") UpdateGroupsForm form,
			Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:/home/";
		}
		
		groupManager.saveAllGroups(assessment, form);
		
		return "redirect:../";
	}
}