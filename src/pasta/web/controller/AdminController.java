/*
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
 * Controller class for admin functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/admin/...
 * <p>
 * Both students and teaching staff can access this url.
 * Students have highly limited functionality. If the authentication
 * system is not using the database, then it's an empty page.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-23
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

	/**
	 * Return the empty form for changing your password (DB authentication only)
	 * 
	 * @return the correct form.
	 */
	@ModelAttribute("changePasswordForm")
	public ChangePasswordForm returnNewUnitTestModel() {
		return new ChangePasswordForm();
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get the current logged in user.
	 * <p>
	 * Gets the "user" attribute from the current session.
	 *  
	 * @see pasta.service.UserManager#getUser(String)
	 * @return the user or null if there is no user with that name.
	 */
	public PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ADMIN //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/admin/
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the login screen.
	 * <p>
	 * Attributes:
	 * <table>
	 * 	<tr><td>authType</td><td>ProjectProperties.getInstance().getAuthenticationValidator().getClass().getName()</td></tr>
	 * 	<tr><td>unikey</td><td>{@link pasta.domain.PASTAUser} currently logged in user</td></tr>
	 * 	<tr><td>people</td><td><b>Only if tutor or instructor!</b> - the list of users (both students and teaching staff)</td></tr>
	 * 	<tr><td>addresses</td><td><b>Only if tutor or instructor!</b> - the list of sever addresses for which to authenticate against</td></tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * 	<li>user/admin</li>
	 * </ul>
	 * 
	 * @param model the model
	 * @return "redirect:/login" or "user/admin".
	 */
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
	
	/**
	 * $PASTAUrl$/admin/changePassword/
	 * <p>
	 * Change the password of a user.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param form the change password form
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/changePassword/", method = RequestMethod.POST)
	public String changePassword(ModelMap model, HttpServletRequest request,
			@ModelAttribute(value = "changePasswordForm") ChangePasswordForm form) {
				
		PASTAUser user = getUser();
		
		if (user != null) {
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
		}
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/replaceStudents/ - POST
	 * <p>
	 * Replace the list of students with a new one. 
	 * Parse the csv format that is passed in (e.g. username,stream,class), turn into
	 * {@link pasta.domain.PASTAUser} and then call {@link pasta.service.UserManager#replaceStudents(List)}.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param list the list of students which will be used to replace (csv format).
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/replaceStudents/", method = RequestMethod.POST)
	public String replaceStudents(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String list) {
				
		PASTAUser user = getUser();
	
		if(user != null && user.isTutor()){
			// replace classlist
			Scanner in = new Scanner(list);
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
			in.close();
			userManager.replaceStudents(users);
		}
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/updateStudents/ - POST
	 * <p>
	 * Update the list of students with a new one. 
	 * Parse the csv format that is passed in (e.g. username,stream,class), turn into
	 * {@link pasta.domain.PASTAUser} and then call {@link pasta.service.UserManager#updateStudents(List)}.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param list the list of students which will be used to replace (csv format).
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/updateStudents/", method = RequestMethod.POST)
	public String updateStudents(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String list) {
				
		PASTAUser user = getUser();
	
		if(user != null && user.isTutor()){
			// update classlist
			Scanner in = new Scanner(list);
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
			in.close();
			userManager.updateStudents(users);
		}
				
		return "redirect:" + request.getHeader("Referer");
	}

	/**
	 * $PASTAUrl$/admin/replaceTutors/ - POST
	 * <p>
	 * Replace the list of teaching staff with a new one. 
	 * Parse the csv format that is passed in (e.g. username,permissionlevel,class1,class2,...,classN), turn into
	 * {@link pasta.domain.PASTAUser} and then call {@link pasta.service.UserManager#replaceTutors(List)}.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param list the list of teaching staff which will be used to replace (csv format).
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/replaceTutors/", method = RequestMethod.POST)
	public String replaceTutors(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String list) {
				
		PASTAUser user = getUser();

		if(user != null && user.isTutor()){
			// replace classlist
			Scanner in = new Scanner(list);
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
			in.close();
			userManager.replaceStudents(users);
		}
				
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/updateTutors/ - POST
	 * <p>
	 * Update the list of teaching staff with a new one. 
	 * Parse the csv format that is passed in (e.g. username,permissionlevel,class1,class2,...,classN), turn into
	 * {@link pasta.domain.PASTAUser} and then call {@link pasta.service.UserManager#updateTutors(List)}.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param list the list of teaching staff which will be used to replace (csv format).
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/updateTutors/", method = RequestMethod.POST)
	public String updateTutors(ModelMap model, HttpServletRequest request,
			@RequestParam(value="list") String list) {
				
		PASTAUser user = getUser();
		
		if(user != null && user.isTutor()){
			// update classlist
			Scanner in = new Scanner(list);
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
				
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/delete/{username}/
	 * <p>
	 * Delete a user from the system.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param username the name of the user
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/delete/{username}/")
	public String deleteUser(ModelMap model, HttpServletRequest request,
			@PathVariable("username") String username) {
				
		PASTAUser user = getUser();
	
		if(user != null && user.isTutor()){
			// update classlist
			PASTAUser toDelete = userManager.getUser(username);
			userManager.deleteUser(toDelete);
		}
				
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/auth/
	 * <p>
	 * Change the authentication system and/or addresses.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param type the authentication type
	 * @param address the addresses which will be used by the authentication
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/auth/", method = RequestMethod.GET)
	public String changeAuthType(ModelMap model, HttpServletRequest request,
			@RequestParam(value="type") String type,
			@RequestParam(value="address") String[] address) {
				
		PASTAUser user = getUser();
	
		if(user != null && user.isTutor()){
			ProjectProperties.getInstance().changeAuthMethod(type, address);
		}
				
		return "redirect:" + request.getHeader("Referer");
	}
}
