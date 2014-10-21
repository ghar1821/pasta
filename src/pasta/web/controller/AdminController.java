/**
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
 */


package pasta.web.controller;


import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.ChangePasswordForm;
import pasta.login.DBAuthValidator;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
@Controller
@RequestMapping("admin/")
public class AdminController {


	protected final Log logger = LogFactory.getLog(getClass());
	private UserManager userManager;

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("changePasswordForm")
	public ChangePasswordForm returnNewUnitTestModel() {
		return new ChangePasswordForm();
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////
	
	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return userManager.getUser(username);
		}
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ADMIN //
	// ///////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String get(ModelMap model) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		
		model.addAttribute("authType", ProjectProperties.getInstance().getAuthenticationValidator().getClass().getName());
		model.addAttribute("unikey", user);
		
		if(user.isTutor()){
			model.addAttribute("people", userManager.getUserList());
			model.addAttribute("addresses", ProjectProperties.getInstance().getServerAddresses());
		}
		
		return "user/admin";
	}
	
	@RequestMapping(value = "/changePassword/", method = RequestMethod.POST)
	public String changePassword(ModelMap model, HttpServletRequest request,
			@ModelAttribute(value = "changePasswordForm") ChangePasswordForm form) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		Validator val = ProjectProperties.getInstance().getAuthenticationValidator();
		if(val instanceof DBAuthValidator){
			DBAuthValidator authenticator = (DBAuthValidator)val;
			if(authenticator.authenticate(user.getUsername(), form.getOldPassword())
					&& form.getNewPassword().equals(form.getConfirmPassword())){
				logger.info("swapping password");
				userManager.updatePassword(user.getUsername(), form.getNewPassword());
			}
			else{
				logger.info("an error occured");
			}
			
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/replaceStudents/", method = RequestMethod.POST)
	public String replaceStudents(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String type) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			// replace classlist
			Scanner in = new Scanner(type);
			List<PASTAUser> users = new LinkedList<PASTAUser>();
			
			while(in.hasNextLine()){
				String line = in.nextLine();
				String[] split = line.split(",");
				if(split.length == 3){
					PASTAUser currUser = new PASTAUser();
					currUser.setUsername(split[0]);
					currUser.setPermissionLevel(UserPermissionLevel.STUDENT);
					currUser.setStream(split[1]);
					currUser.setTutorial(split[2]);
					
					users.add(currUser);
				}
			}
			userManager.replaceStudents(users);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/updateStudents/", method = RequestMethod.POST)
	public String updateStudents(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String type) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			// update classlist
			Scanner in = new Scanner(type);
			List<PASTAUser> users = new LinkedList<PASTAUser>();
			
			while(in.hasNextLine()){
				String line = in.nextLine();
				String[] split = line.split(",");
				if(split.length == 3){
					PASTAUser currUser = new PASTAUser();
					currUser.setUsername(split[0]);
					currUser.setPermissionLevel(UserPermissionLevel.STUDENT);
					currUser.setStream(split[1]);
					currUser.setTutorial(split[2]);
					
					users.add(currUser);
				}
			}
			userManager.updateStudents(users);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}

	@RequestMapping(value = "/replaceTutors/", method = RequestMethod.POST)
	public String replaceTutors(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String type) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			// replace classlist
			Scanner in = new Scanner(type);
			List<PASTAUser> users = new LinkedList<PASTAUser>();
			
			while(in.hasNextLine()){
				String line = in.nextLine();
				String[] split = line.split(",");
				if(split.length >= 3){
					PASTAUser currUser = new PASTAUser();
					String classes = new String(line);
					currUser.setUsername(split[0]);
					currUser.setPermissionLevel(UserPermissionLevel.STUDENT);
					classes.replaceFirst(split[0]+",", "");
					if(split[1].trim().toLowerCase().equals("instructor")){
						currUser.setPermissionLevel(UserPermissionLevel.INSTRUCTOR);
					}
					else if(split[1].trim().toLowerCase().equals("tutor")){
						currUser.setPermissionLevel(UserPermissionLevel.TUTOR);
					}
					else{
						currUser.setPermissionLevel(UserPermissionLevel.STUDENT);
					}
					currUser.setStream("");
					classes.replaceFirst(split[1]+",", "");
					currUser.setTutorial(classes);
					
					users.add(currUser);
				}
			}
			userManager.replaceStudents(users);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/updateTutors/", method = RequestMethod.POST)
	public String updateTutors(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String type) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			// update classlist
			Scanner in = new Scanner(type);
			List<PASTAUser> users = new LinkedList<PASTAUser>();
			
			while(in.hasNextLine()){
				String line = in.nextLine();
				String[] split = line.split(",");
				if(split.length >= 3){
					PASTAUser currUser = new PASTAUser();
					String classes = new String(line);
					currUser.setUsername(split[0]);
					currUser.setPermissionLevel(UserPermissionLevel.STUDENT);
					classes = classes.replaceFirst(split[0]+",", "");
					if(split[1].trim().toLowerCase().equals("instructor")){
						currUser.setPermissionLevel(UserPermissionLevel.INSTRUCTOR);
					}
					else if(split[1].trim().toLowerCase().equals("tutor")){
						currUser.setPermissionLevel(UserPermissionLevel.TUTOR);
					}
					else{
						currUser.setPermissionLevel(UserPermissionLevel.STUDENT);
					}
					currUser.setStream("");
					classes = classes.replaceFirst(split[1]+",", "");
					currUser.setTutorial(classes);
					
					users.add(currUser);
				}
			}
			userManager.updateTutors(users);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/delete/{username}/")
	public String deleteUser(ModelMap model, HttpServletRequest request,
			@PathVariable("username") String username) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			// update classlist
			PASTAUser toDelete = new PASTAUser();
			toDelete.setUsername(username);
			userManager.deleteUser(toDelete);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/auth/", method = RequestMethod.GET)
	public String changeAuthType(ModelMap model, HttpServletRequest request,
			@RequestParam(value="type") String type,
			@RequestParam(value="address") String[] address) {
				
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/../";
		}
	
		if(user.isTutor()){
			ProjectProperties.getInstance().changeAuthMethod(type, address);
		}
				
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}
}
