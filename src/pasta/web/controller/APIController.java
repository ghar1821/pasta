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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.ResponseBody;

import pasta.domain.form.LoginForm;
import pasta.domain.result.CombinedAssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.security.TokenUtils;
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
	@Autowired
	private TokenUtils tokenUtils;

	@RequestMapping(value="authenticate/", method=RequestMethod.POST)
	@ResponseBody
	public String getToken(HttpServletRequest request, @RequestParam("username") String username, @RequestParam("password") String password) {
		return tokenUtils.generateToken(request, username, password);
	}
	
	@RequestMapping(value="testToken/", method=RequestMethod.GET)
	@ResponseBody
	public String testToken() {
		return "VALID";
	}

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
				Map<PASTAUser, Map<Long, CombinedAssessmentResult>> latestResults = resultManager
						.getLatestResultsIncludingGroup(userManager.getUserList());

				for (Entry<PASTAUser, Map<Long, CombinedAssessmentResult>> entry : latestResults
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