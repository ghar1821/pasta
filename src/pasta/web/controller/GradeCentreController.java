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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import pasta.domain.UserPermissionLevel;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.ResultManager;
import pasta.service.UserManager;
import pasta.view.ExcelAutoMarkView;
import pasta.view.ExcelMarkView;
import pasta.web.WebUtils;

/**
 * Controller class for the gradeCentre functions.
 * <p>
 * Handles mappings of $PASTAUrl$/gradeCentre/...
 * <p>
 * 
 * @author Martin McGrane
 * @version 1.0
 * @since 23 Sep 2016
 */
@Controller
@RequestMapping("gradeCentre/")
public class GradeCentreController {

	public GradeCentreController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private ResultManager resultManager;
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/downloadMarks/
	 * <p>
	 * Download the marks as an excel sheet. If the user has not authenticated:
	 * redirect to login. If the user is not a tutor: redirect to home.
	 * 
	 * @param request the http request that is kinda not used.
	 * @param response also not really used
	 * @return the model and view (which is actually a
	 *         {@link pasta.view.ExcelMarkView})
	 */
	@RequestMapping(value = "downloadMarks/")
	public ModelAndView viewExcel(@ModelAttribute("user") PASTAUser user, HttpServletResponse response,
			@RequestParam(value = "myClasses", required = false) Boolean useMyClasses,
			@RequestParam(value = "tutorial", required = false) String tutorial,
			@RequestParam(value = "stream", required = false) String stream) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		Map<String, Object> data = new TreeMap<String, Object>();

		data.put("assessmentList", assessmentManager.getAssessmentList());

		Collection<PASTAUser> userList;
		if (useMyClasses != null) {
			userList = userManager.getTutoredStudents(user);
		} else if (stream != null) {
			userList = userManager.getUserListByStream(stream);
		} else if (tutorial != null) {
			userList = userManager.getUserListByTutorial(tutorial);
		} else {
			userList = userManager.getUserList();
		}
		data.put("userList", userList);
		data.put("latestResults", resultManager.getLatestResultsIncludingGroupQuick(userList));

		return new ModelAndView(new ExcelMarkView(), data);
	}

	/**
	 * $PASTAUrl$/downloadAutoMarks/
	 * <p>
	 * Download the only the automated marks (all but hand marking) as an excel
	 * sheet. If the user has not authenticated: redirect to login. If the user is
	 * not a tutor: redirect to home.
	 * 
	 * @param request the http request that is kinda not used.
	 * @param response also not really used
	 * @return the model and view (which is actually a
	 *         {@link pasta.view.ExcelMarkView})
	 */
	@RequestMapping(value = "downloadAutoMarks/")
	public ModelAndView viewAutoExcel(@ModelAttribute("user") PASTAUser user, HttpServletResponse response,
			@RequestParam(value = "myClasses", required = false) Boolean useMyClasses,
			@RequestParam(value = "tutorial", required = false) String tutorial,
			@RequestParam(value = "stream", required = false) String stream) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		Map<String, Object> data = new TreeMap<String, Object>();

		data.put("assessmentList", assessmentManager.getAssessmentList());

		Collection<PASTAUser> userList;
		if (useMyClasses != null) {
			userList = userManager.getTutoredStudents(user);
		} else if (stream != null) {
			userList = userManager.getUserListByStream(stream);
		} else if (tutorial != null) {
			userList = userManager.getUserListByTutorial(tutorial);
		} else {
			userList = userManager.getUserList();
		}
		data.put("userList", userList);
		data.put("latestResults", resultManager.getLatestResultsIncludingGroupQuick(userList));

		return new ModelAndView(new ExcelAutoMarkView(), data);
	}

	// ///////////////////////////////////////////////////////////////////////////
	// GRADE CENTRE //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center. If the user has not
	 * authenticated or is not a tutor: return nothing. Otherwise use
	 * {@link GradeCentreController#generateJSON(PASTAUser[])} for all users.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "DATA/")
	public @ResponseBody String viewGradeCentreData() {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		return generateJSON(userManager.getStudentList());
	}

	/**
	 * $PASTAUrl$/stream/{streamName}/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for a stream. If the
	 * user has not authenticated or is not a tutor: return nothing. Otherwise use
	 * {@link GradeCentreController#generateJSON(PASTAUser[])} for all users in
	 * the given stream. Return nothing if the stream doesn't exist.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "stream/{streamName}/DATA/")
	public @ResponseBody String viewStreamData(@PathVariable("streamName") String streamName) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		if (userManager.getUserListByStream(streamName) == null) {
			return "";
		}
		return generateJSON(userManager.getUserListByStream(streamName));
	}

	/**
	 * $PASTAUrl$/tutorial/{className}/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for a tutorial
	 * class. If the user has not authenticated or is not a tutor: return nothing.
	 * Otherwise use {@link GradeCentreController#generateJSON(PASTAUser[])} for
	 * all users in the given tutorial class. Return nothing if the tutorial class
	 * doesn't exist.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "tutorial/{className}/DATA/")
	public @ResponseBody String viewTutorialData(@PathVariable("className") String className) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		if (userManager.getUserListByTutorial(className) == null) {
			return "";
		}

		return generateJSON(userManager.getUserListByTutorial(className));
	}

	/**
	 * $PASTAUrl$/myTutorial/DATA/
	 * <p>
	 * Serves up the JSON data container for the grade center for the user's
	 * tutorial class. If the user has not authenticated or is not a tutor: return
	 * nothing. Otherwise use
	 * {@link GradeCentreController#generateJSON(PASTAUser[])} for all users in
	 * the user's tutorial class.
	 * 
	 * @return the appropriate json file
	 */
	@RequestMapping(value = "myTutorials/DATA/")
	public @ResponseBody String viewMyTutorialData(@ModelAttribute("user") PASTAUser user) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		Collection<PASTAUser> myUsers = userManager.getTutoredStudents(user);
		return generateJSON(myUsers);
	}

	/**
	 * Generate the JSON
	 * <p>
	 * format:
	 * 
	 * <pre>
	 * {@code {
	 * 	"data": 
	 * 	[
	 * 		{
	 * 			"name": "$username$",
	 * 			"stream": "$stream$",
	 * 			"class": "$class$",
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 			},
	 * 			...
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 			}
	 * 		},
	 * 		...
	 * 		{
	 * 			"name": "$username$",
	 * 			"stream": "$stream$",
	 * 			"class": "$class$",
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 			},
	 * 			...
	 * 			"$assessmentId$": {
	 * 				"mark": "######.###",
	 * 				"percentage": "double",
	 * 				"assessmentid": "$assessmentId$"
	 * 			}
	 * 		}
	 * 	]
	 * }}
	 * </pre>
	 * 
	 * If there is no submission, mark and percentage will be "". Percentage is
	 * [1.0,0.0]. Mark is displayed to 3 decimal places.
	 * 
	 * @param allUsers the users for which to generate the JSON
	 * @return the JSON string
	 */
	private String generateJSON(Collection<PASTAUser> allUsers) {
		if (allUsers.isEmpty()) {
			return "{\"data\": []}";
		}

		List<PASTAUser> usersList = new ArrayList<>(allUsers);

		DecimalFormat df = new DecimalFormat("#.###");

		StringBuilder data = new StringBuilder("{\r\n  \"data\": [\r\n");

		Map<PASTAUser, Map<Long, Double>> allResults = resultManager
				.getLatestResultsIncludingGroupEvenQuicker(usersList);

		Assessment[] allAssessments = assessmentManager.getAssessmentList().toArray(new Assessment[0]);
		for (int i = 0; i < usersList.size(); ++i) {
			PASTAUser user = usersList.get(i);

			data.append("    {\r\n");

			// name
			data.append("      \"name\": \"" + user.getUsername() + "\",\r\n");
			// stream
			data.append("      \"stream\": \"" + user.getStream() + "\",\r\n");
			// class
			data.append("      \"class\": \"" + user.getFullTutorial() + "\"");

			if (allAssessments.length > 0) {
				data.append(",");
			}
			data.append("\r\n");

			Map<Long, Double> userResults = allResults.get(user);
			// marks
			for (int j = 0; j < allAssessments.length; j++) {
				// assessment mark
				Assessment currAssessment = allAssessments[j];
				data.append("      \"" + currAssessment.getId() + "\": {");
				String mark = "";
				String percentage = "";

				Double latestResult = userResults == null ? null : userResults.get(currAssessment.getId());
				if (latestResult != null) {
					percentage = String.valueOf(latestResult);
					mark = df.format(latestResult * currAssessment.getMarks());
				}
				data.append("\"mark\": \"" + mark + "\",");
				data.append("\"percentage\": \"" + percentage + "\",");
				data.append("\"max\": \"" + currAssessment.getMarks() + "\",");
				data.append("\"assessmentid\": \"" + currAssessment.getId() + "\"");
				data.append("}");

				if (j < allAssessments.length - 1) {
					data.append(",");
				}
			}

			data.append("}");
			if (i < usersList.size() - 1) {
				data.append(",");
			}
			data.append("\r\n");
		}
		data.append("  ]\r\n}");
		return data.toString();
	}

	/**
	 * $PASTAUrl$/
	 * <p>
	 * Display the grade centre. If the user has not authenticated: redirect to
	 * login. If the user is not a tutor: redirect to home. ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>user/gradeCentre</li>
	 * </ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/gradeCentre"
	 */
	@RequestMapping(value = "")
	public String viewGradeCentre(Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
		model.addAttribute("pathBack", ".");

		return "user/gradeCentre";
	}

	/**
	 * $PASTAUrl$/tutorial/{className}/
	 * <p>
	 * Display the grade center for a given class. If the user has not
	 * authenticated: redirect to login. If the user is not a tutor: redirect to
	 * home. ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>user/gradeCentre</li>
	 * </ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/gradeCentre"
	 */
	@RequestMapping(value = "tutorial/{className}/")
	public String viewClass(@PathVariable("className") String className, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
		model.addAttribute("pathBack", "../..");
		model.addAttribute("tutorial", className);

		return "user/gradeCentre";
	}

	/**
	 * $PASTAUrl$/stream/{streamName}/
	 * <p>
	 * Display the grade center for a given stream. If the user has not
	 * authenticated: redirect to login. If the user is not a tutor: redirect to
	 * home. ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>user/gradeCentre</li>
	 * </ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/gradeCentre"
	 */
	@RequestMapping(value = "stream/{streamName}/")
	public String viewStream(@PathVariable("streamName") String streamName, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
		model.addAttribute("pathBack", "../..");
		model.addAttribute("stream", streamName);

		return "user/gradeCentre";
	}

	/**
	 * $PASTAUrl$/myTutorials/
	 * <p>
	 * Display the grade center for the user's tutorial(s). If the user has not
	 * authenticated: redirect to login. If the user is not a tutor: redirect to
	 * home. ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessmentList</td>
	 * <td>The list of all {@link pasta.domain.template.Assessment} on the
	 * system</td>
	 * </tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * <li>user/gradeCentre</li>
	 * </ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "user/gradeCentre"
	 */
	@RequestMapping(value = "myTutorials/")
	public String viewMyTutorials(Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);

		model.addAttribute("assessmentList", assessmentManager.getAssessmentList());
		model.addAttribute("pathBack", "..");
		model.addAttribute("myClasses", true);

		return "user/gradeCentre";
	}

}
