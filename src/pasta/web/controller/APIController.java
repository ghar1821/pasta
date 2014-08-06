package pasta.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

import pasta.domain.PASTAUser;
import pasta.domain.form.LoginForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.service.AssessmentManager;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

@Controller
@RequestMapping("api/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class APIController {

	protected final Log logger = LogFactory.getLog(getClass());

	private AssessmentManager assessmentManager;
	private UserManager userManager;

	@Autowired
	public void setMyService(AssessmentManager myService) {
		this.assessmentManager = myService;
	}

	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	
	@RequestMapping(value = "latestMarks", method = RequestMethod.GET)
	public void viewGradeCentreAPI(HttpServletResponse response,
			@RequestParam("username") String username,
			@RequestParam("password") String password) {

		LoginForm userMsg = new LoginForm();
		userMsg.setUnikey(username);
		userMsg.setPassword(password);

		Errors result = (Errors) new BindException("", "");

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
				Map<String, Map<String, AssessmentResult>> latestResults = assessmentManager
						.getLatestResults(userManager.getUserList());

				for (Entry<String, Map<String, AssessmentResult>> entry : latestResults
						.entrySet()) {
					PASTAUser user = userManager.getUser(entry.getKey());
					if (user != null) {
						out.print(user.getUsername() + "," + user.getStream()
								+ "," + user.getTutorial());
					}

					for (Assessment assessment : assessments) {
						if (entry.getValue() == null ||
								entry.getValue().get(assessment.getShortName()) == null) {
							out.print(",-");
						} else {
							out.print(","
									+ entry.getValue()
											.get(assessment.getShortName())
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