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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.docker.CommandResult;
import pasta.docker.DockerManager;
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.ChangePasswordForm;
import pasta.domain.form.UpdateOptionsForm;
import pasta.domain.form.UpdateUsersForm;
import pasta.domain.form.validate.UpdateOptionsFormValidator;
import pasta.domain.form.validate.UpdateUsersFormValidator;
import pasta.domain.user.PASTAUser;
import pasta.login.DBAuthValidator;
import pasta.service.ExecutionManager;
import pasta.service.PASTAOptions;
import pasta.service.UserManager;
import pasta.service.reporting.CSVReport;
import pasta.service.reporting.CSVReport.CSVPage;
import pasta.service.reporting.UnitTestReportingManager;
import pasta.util.ProjectProperties;
import pasta.util.WhichProgram;
import pasta.web.WebUtils;

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
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private ExecutionManager executionManager;
	
	@Autowired
	private UnitTestReportingManager unitTestReportingManager;
	
	@Autowired
	private UpdateUsersFormValidator updateValidator;
	
	@Autowired
	private UpdateOptionsFormValidator updateOptionsValidator;
	
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
	
	@ModelAttribute("updateUsersForm")
	public UpdateUsersForm returnUpdateUsersForm() {
		return new UpdateUsersForm();
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}
	
	@ModelAttribute("updateOptionsForm")
	public UpdateOptionsForm getUpdateOptionsForm() {
		return new UpdateOptionsForm(PASTAOptions.instance().getAllOptions());
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
	 * 	<tr><td>people</td><td><b>Only if tutor or instructor!</b> - the list of users (both students and teaching staff)</td></tr>
	 * 	<tr><td>addresses</td><td><b>Only if tutor or instructor!</b> - the list of sever addresses for which to authenticate against</td></tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * 	<li>admin/admin</li>
	 * </ul>
	 * 
	 * @param model the model
	 * @return "redirect:/login" or "admin/admin".
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String viewAdmin(@ModelAttribute("user") PASTAUser user, ModelMap model) {
		model.addAttribute("authType", ProjectProperties.getInstance().getAuthenticationValidator().getClass().getName());
		if(user.isTutor()){
			model.addAttribute("people", userManager.getUserList());
			model.addAttribute("addresses", ProjectProperties.getInstance().getAuthenticationSettings().getServerAddresses());
			model.addAttribute("taskDetails", executionManager.getExecutingTaskDetails());
		}
		return "admin/admin";
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
	public String changePassword(@ModelAttribute("user") PASTAUser user, ModelMap model, HttpServletRequest request,
			@ModelAttribute(value = "changePasswordForm") ChangePasswordForm form) {
				
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
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/updateUsers/ - POST
	 * <p>
	 * Updates the list of either students or tutors/instructors. This may be an update or replace operation.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/updateUsers/", method = RequestMethod.POST)
	public String updateUsers(
			@Valid @ModelAttribute("updateUsersForm") UpdateUsersForm form, BindingResult result, 
			RedirectAttributes attr, Model model, HttpServletRequest request) {
		if(form.isUpdateTutors()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
		
		updateValidator.validate(form, result);
		if(result.hasErrors()) { 
			attr.addFlashAttribute("updateUsersForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.updateUsersForm", result);
			return "redirect:../.";
		}
		
		userManager.updateUsers(form);
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/delete/{deleteUsername}/
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
	 * @param deleteUsername the name of the user
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/delete/{deleteUsername}/")
	public String deleteUser(ModelMap model, HttpServletRequest request,
			@PathVariable("deleteUsername") String username) {
				
		PASTAUser toDelete = userManager.getUser(username);
		if(toDelete.isTutor()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
	
		userManager.deleteUser(toDelete);
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
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
	
		ProjectProperties.getInstance().changeAuthMethod(type, address);
		return "redirect:" + request.getHeader("Referer");
	}
	
	@RequestMapping(value = "/forceSubmissionRefresh/", method = RequestMethod.POST)
	public String forceSubmissionRefresh(HttpServletRequest request) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		executionManager.forceSubmissionRefresh();
		return "redirect:" + request.getHeader("Referer");
	}
	
	@RequestMapping(value = "/options/", method = RequestMethod.GET)
	public String loadOptions() {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		return "admin/options";
	}
	
	@RequestMapping(value = "/options/updateOptions/", method = RequestMethod.POST)
	public String updateOptions(HttpServletRequest request, 
			@Valid @ModelAttribute("updateOptionsForm") UpdateOptionsForm form, BindingResult result, 
			RedirectAttributes attr) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		updateOptionsValidator.validate(form, result);
		if(result.hasErrors()) { 
			attr.addFlashAttribute("updateOptionsForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.updateOptionsForm", result);
			return "redirect:../.";
		}
		
		PASTAOptions.instance().updateOptions(form);
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	@RequestMapping(value = "/downloads/", method = RequestMethod.GET)
	public String viewDownloads() {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		return "admin/downloads";
	}
	
	@Autowired
	private DataSource dataSource;
	
	@RequestMapping(value = "/downloads/dbdump/", method = RequestMethod.POST, produces="application/zip")
	public void downloadDatabaseDump(HttpServletRequest request, HttpServletResponse response) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		DBInfo info = new DBInfo((BasicDataSource) dataSource);
		List<String> command = new LinkedList<>();
		
		command.add(WhichProgram.getInstance().path("mysqldump"));
		command.add("--user=" + info.username);
		command.add("--password=" + info.password);
		command.add("--lock-tables");
				
		String[] ignoreTables = {
				"user_logins",
				"assessment_ratings",
				"hibernate_sequences",
				"authentication_settings",
				"server_addresses"
		};
		for(String ignore : ignoreTables) {
			command.add("--ignore-table=" + info.databaseName + "." + ignore);
		}
		
		command.add(info.databaseName);
		
		CommandResult result = null;
		try {
			result = DockerManager.instance().executeDatabaseDump(command);
			if(!result.getError().isEmpty()) {
				throw new IOException(result.getError());
			}
		} catch (IOException e) {
			logger.error("Error generating SQL dump:", e);
		}
		
		String filename = "pasta_" + new SimpleDateFormat("YYYY-MM-dd").format(new Date());
	    response.setHeader("Content-disposition", "attachment; filename=" + filename + ".zip");

	    try {
	    	// Zip the file
	    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    	ZipOutputStream zip = new ZipOutputStream(outStream);
	    	
	    	ZipEntry ze = new ZipEntry(filename + ".sql");
			zip.putNextEntry(ze);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(result.getOutput().getBytes());
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bais.read(buffer)) > 0) {
				zip.write(buffer, 0, len);
			}
			bais.close();
			zip.closeEntry();
			zip.close();
			
			// Send the file
			OutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(outStream.toByteArray());
			IOUtils.copy(in,out);
		} catch (IOException e) {
			logger.error("Error sending SQL dump:", e);
		}
	}
	
	private static class DBInfo {
		String username;
		String password;
		String hostname;
		String port;
		String databaseName;
		public DBInfo(BasicDataSource ds) {
			this.username = ds.getUsername();
			this.password = ds.getPassword();
			String url = ds.getUrl();
			int end = url.indexOf("//");
			int start = 0;
			if(end >= 0) {
				start = end + 2;
				end = url.indexOf(':', start);
				this.hostname = url.substring(start, end);
				start = end + 1;
				end = url.indexOf('/', start);
				this.port = url.substring(start, end);
				start = end + 1;
				end = url.indexOf('?', start);
				if(end >= 0) {
					this.databaseName = url.substring(start, end);
				} else {
					this.databaseName = url.substring(start);
				}
			}
		}
	}
	
	@RequestMapping(value = "/downloads/utchistory/", method = RequestMethod.POST, produces="application/zip")
	public void downloadUnitTestCaseHistory(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value="maxRowCount", required=false) int maxRowCount) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		CSVReport report = unitTestReportingManager.getAllUnitTestAttemptsReport(maxRowCount);
		downloadCSVReport(report, response, "pasta_unit_test_results");
	}
	
	@RequestMapping(value = "/downloads/submissionhistory/", method = RequestMethod.POST, produces="application/zip")
	public void downloadSubmissionHistory(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value="maxRowCount", required=false) int maxRowCount) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		CSVReport report = unitTestReportingManager.getAllSubmissionsReport(maxRowCount);
		downloadCSVReport(report, response, "pasta_submissions");
	}
	
	private void downloadCSVReport(CSVReport report, HttpServletResponse response, String filePrefix) {
		CSVPage[] pages = report.getPages();
		
		String filename = filePrefix + "_" + new SimpleDateFormat("YYYY-MM-dd").format(new Date());
	    response.setHeader("Content-disposition", "attachment; filename=" + filename + ".zip");

	    try {
	    	// Zip the file
	    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    	ZipOutputStream zip = new ZipOutputStream(outStream);
	    	
	    	for(int i = 0; i < pages.length; i++) {
	    		String entryName = "results";
	    		if(pages.length > 1) {
	    			int numDigits = (int) Math.ceil(Math.log10(pages.length + 1));
	    			entryName += String.format("%0" + numDigits + "d", i + 1);
	    		}
	    		entryName += ".csv";
	    		ZipEntry ze = new ZipEntry(entryName);
				zip.putNextEntry(ze);
				pages[i].output(zip);
				zip.closeEntry();
	    	}
	    	
			zip.close();
			
			// Send the file
			OutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(outStream.toByteArray());
			IOUtils.copy(in,out);
		} catch (IOException e) {
			logger.error("Error sending CSV report:", e);
		}
	}
}
