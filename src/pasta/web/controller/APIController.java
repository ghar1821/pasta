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
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pasta.domain.form.LoginForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.ResultManager;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

/**
 * Controller class for API functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/api/...
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-22
 *
 */
@Controller
@RequestMapping("api/")
public class APIController {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResultManager resultManager;

	/**
	 * Get the latest marks
	 * <p>
	 * Authenticate against the system, if successful, get the latest marks as a csv.
	 * In cases where there are no attempts, put "-".
	 * 
	 * @param response the http response which will be used
	 * @param username the username in plaintext
	 * @param password the password in plaintext
	 */
	@RequestMapping(value = "latestMarks", method = RequestMethod.GET)
	public void viewGradeCentreAPI(HttpServletResponse response,
			@RequestParam("username") String username,
			@RequestParam("password") String password) {

		LoginForm userMsg = new LoginForm();
		userMsg.setUnikey(username);
		userMsg.setPassword(password);

		Errors result = new BindException("", "");

		ProjectProperties.getInstance().getAuthenticationValidator()
				.validate(userMsg, result);
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream, true);
			
			if (!result.hasErrors() && userManager.getUser(username) != null
					&& userManager.getUser(username).isTutor()) {

				out.print("username,stream,class");

				Collection<Assessment> assessments = assessmentManager
						.getAssessmentList();
				for (Assessment assessment : assessments) {
					out.print("," + assessment.getName());
				}
				out.println();

				// username, assessment name, result
				Map<PASTAUser, Map<Long, AssessmentResult>> latestResults = resultManager
						.getLatestResults(userManager.getUserList());

				for (Entry<PASTAUser, Map<Long, AssessmentResult>> entry : latestResults
						.entrySet()) {
					PASTAUser user = entry.getKey();
					if (user != null) {
						out.print(user.getUsername() + "," + user.getStream()
								+ "," + user.getTutorial());
					}

					for (Assessment assessment : assessments) {
						if (entry.getValue() == null ||
								entry.getValue().get(assessment.getId()) == null) {
							out.print(",-");
						} else {
							out.print(","
									+ entry.getValue()
											.get(assessment.getId())
											.getMarks());
						}
					}
					out.println();
				}
			}
			else{
				out.println("username or password is incorrect");
			}
			IOUtils.copy(new ByteArrayInputStream(outStream.toByteArray()),
					response.getOutputStream());
			response.flushBuffer();
			out.close();

		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}
	}

}