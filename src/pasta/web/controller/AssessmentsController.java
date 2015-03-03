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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.form.ReleaseForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.service.AssessmentManager;
import pasta.service.CompetitionManager;
import pasta.service.HandMarkingManager;
import pasta.service.SubmissionManager;
import pasta.service.UnitTestManager;
import pasta.service.UserManager;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

/**
 * Controller class for Assessment functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/assessments/...
 * <p>
 * Only teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("assessments/")
public class AssessmentsController {

	/**
	 * Initializes the codeStyle tag mapping of file endings to 
	 * javascript tag requirements for syntax highlighting.
	 */
	public AssessmentsController() {
		codeStyle = new TreeMap<String, String>();
		codeStyle.put("c", "ccode");
		codeStyle.put("cpp", "cppcode");
		codeStyle.put("h", "cppcode");
		codeStyle.put("cs", "csharpcode");
		codeStyle.put("css", "csscode");
		codeStyle.put("html", "htmlcode");
		codeStyle.put("java", "javacode");
		codeStyle.put("js", "javascriptcode");
		codeStyle.put("pl", "perlcode");
		codeStyle.put("pm", "perlcode");
		codeStyle.put("php", "phpcode");
		codeStyle.put("py", "pythoncode");
		codeStyle.put("rb", "rubycode");
		codeStyle.put("sql", "sqlcode");
		codeStyle.put("xml", "xmlcode");

	}

	protected final Log logger = LogFactory.getLog(getClass());

	private UserManager userManager;
	private AssessmentManager assessmentManager;
	private UnitTestManager unitTestManager;
	private HandMarkingManager handMarkingManager;
	private CompetitionManager competitionManager;
	private SubmissionManager submissionManager;

	private Map<String, String> codeStyle;

	@Autowired
	public void setMyService(CompetitionManager myService) {
		this.competitionManager = myService;
	}
	
	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.submissionManager = myService;
	}
	
	@Autowired
	public void setMyService(UserManager myService) {
		this.userManager = myService;
	}
	
	@Autowired
	public void setMyService(AssessmentManager myService) {
		this.assessmentManager = myService;
	}
	
	@Autowired
	public void setMyService(UnitTestManager myService) {
		this.unitTestManager = myService;
	}
	
	@Autowired
	public void setMyService(HandMarkingManager myService) {
		this.handMarkingManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////


	@ModelAttribute("assessment")
	public Assessment returnAssessmentModel() {
		return new Assessment();
	}

	@ModelAttribute("assessmentRelease")
	public ReleaseForm returnAssessmentReleaseModel() {
		return new ReleaseForm();
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get or create the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in.
	 */
	public PASTAUser getOrCreateUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return userManager.getOrCreateUser(username);
		}
		return null;
	}

	/**
	 * Get the currently logged in user. If it doesn't exist, don't create it.
	 * 
	 * @return the currently logged in user, null if not logged in or doesn't already exist.
	 */
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
	// ASSESSMENTS //
	// ///////////////////////////////////////////////////////////////////////////


	/**
	 * $PASTAUrl$/assessments/{assessmentName}/ - GET
	 * <p>
	 * View the assessment details.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * Otherwise:
	 * 
	 * Wrap the weighted containers around all assessment modules (Unit tets, hand
	 * marking, competitions ...)
	 * Add them to the model for use.
	 * 
	 * Attributes:
	 * <table>
	 * 	<tr><td>assessment</td><td>the corresponding {@link pasta.domain.template.Assessment}</td></tr>
	 * 	<tr><td>tutorialByStream</td><td>A map of the streams and which tutorials belong to them. Used for releases.</td></tr>
	 * 	<tr><td>otherUnitTests</td><td>The weighted unit tests not already associated with this assessment</td></tr>
	 * 	<tr><td>otherHandMarking</td><td>The weighted hand marking templates not already associated with this assessment</td></tr>
	 * 	<tr><td>otherCompetitions</td><td>The weighted competitions not already associated with this assessment</td></tr>
	 * 	<tr><td>unikey</td><td>the username of the current logged in user</td></tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul><li>assessment/view/assessment</li></ul>
	 * 
	 * @param assessmentName the short name (no whitespace) of the assessment.
	 * @param model the model used to add attributes
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/assessment"
	 */
	@RequestMapping(value = "{assessmentName}/")
	public String viewAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		Assessment currAssessment = assessmentManager.getAssessment(assessmentName);
		model.addAttribute("assessment", currAssessment);

		List<WeightedUnitTest> otherUnitTetsts = new LinkedList<WeightedUnitTest>();

		for (UnitTest test : unitTestManager.getUnitTestList()) {
			boolean contains = false;
			for (WeightedUnitTest weightedTest : currAssessment.getUnitTests()) {
				if (weightedTest.getTest() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				for (WeightedUnitTest weightedTest : currAssessment
						.getSecretUnitTests()) {
					if (weightedTest.getTest() == test) {
						contains = true;
						break;
					}
				}
			}

			if (!contains) {
				WeightedUnitTest weigthedTest = new WeightedUnitTest();
				weigthedTest.setTest(test);
				weigthedTest.setWeight(0);
				otherUnitTetsts.add(weigthedTest);
			}
		}

		List<WeightedHandMarking> otherHandMarking = new LinkedList<WeightedHandMarking>();

		for (HandMarking test : handMarkingManager.getHandMarkingList()) {
			boolean contains = false;
			for (WeightedHandMarking weightedHandMarking : currAssessment
					.getHandMarking()) {
				if (weightedHandMarking.getHandMarking() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				WeightedHandMarking weigthedHM = new WeightedHandMarking();
				weigthedHM.setHandMarking(test);
				weigthedHM.setWeight(0);
				otherHandMarking.add(weigthedHM);
			}
		}

		List<WeightedCompetition> otherCompetitions = new LinkedList<WeightedCompetition>();

		for (Competition test : competitionManager.getCompetitionList()) {
			boolean contains = false;
			for (WeightedCompetition weightedComp : currAssessment
					.getCompetitions()) {
				if (weightedComp.getCompetition() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				WeightedCompetition weightedComp = new WeightedCompetition();
				weightedComp.setCompetition(test);
				weightedComp.setWeight(0);
				otherCompetitions.add(weightedComp);
			}
		}

		model.addAttribute("tutorialByStream", userManager.getTutorialByStream());
		model.addAttribute("otherUnitTests", otherUnitTetsts);
		model.addAttribute("otherHandMarking", otherHandMarking);
		model.addAttribute("otherCompetitions", otherCompetitions);
		model.addAttribute("unikey", user);
		return "assessment/view/assessment";
	}
	
	/**
	 * $PASTAUrl$/assessments/{assessmentName}/ - POST
	 * <p>
	 * Update an assessment. Only instructors can change an assessment.
	 * Tutors can only view.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor, update the assessment by calling 
	 * {@link pasta.service.AssessmentManager#addAssessment(Assessment)}
	 * 
	 * Redirect back to the post version of this page.
	 * 
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param form the form for updating the assessment
	 * @param result the binding result, used for feedback
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "{assessmentName}/", method = RequestMethod.POST)
	public String updateAssessment(
			@PathVariable("assessmentName") String assessmentName,
			@ModelAttribute(value = "assessment") Assessment form,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (user.isInstructor()) {
			form.setName(assessmentManager.getAssessment(assessmentName).getName());
			assessmentManager.addAssessment(form);
		}
		return "redirect:.";
	}

	/**
	 * $PASTAUrl$/assessments/{assessmentName}/run/
	 * <p>
	 * Schedule the execution of an assessment for all students who have submitted.
	 * Only works for instructors.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor, schedule for execution using 
	 * {@link pasta.service.SubmissionManager#runAssessment(Assessment, java.util.Collection)}
	 * redirect to the referrer.
	 * 
	 * @param assessmentName the name of the assessment
	 * @param request the http request, used for redirection
	 * @return "redirect:/login/" or "redirect:/home/" or redirect to referrer
	 */
	@RequestMapping(value = "{assessmentName}/run/")
	public String runAssessment(
			@PathVariable("assessmentName") String assessmentName,
			HttpServletRequest request) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (user.isInstructor()) {
			submissionManager.runAssessment(assessmentManager.getAssessment(assessmentName), userManager.getUserList());
		}
		return "redirect:" + request.getHeader("Referer");
	}

	/**
	 * $PASTAUrl$/assessments/
	 * <p>
	 * View the list of all assessments.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * <p>
	 * Attributes:
	 * <table>
	 * 	<tr><td>tutorialByStream</td><td>All tutorials by stream, used for releases from this page</td></tr>
	 * 	<tr><td>allAssessments</td><td>All assessments</td></tr>
	 * 	<tr><td>unikey</td><td>the user viewing this page.</td></tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul><li>assessment/viewAll/assessment</li></ul>
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/viewAll/assessment" 
	 */
	@RequestMapping(value = "")
	public String viewAllAssessment(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		model.addAttribute("tutorialByStream", userManager.getTutorialByStream());
		model.addAttribute("allAssessments", assessmentManager.getAssessmentList());
		model.addAttribute("unikey", user);
		return "assessment/viewAll/assessment";
	}

	/**
	 * $PASTAUrl$/assessments/ - POST
	 * <p>
	 * Add a new assessment
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor: 
	 * <ol>
	 * 	<li>Check if the assessment has a name given, if it does reject the form with the reason "Assessment.new.noname".</li>
	 * 	<li>If the assessment has a name, add one using {@link pasta.service.AssessmentManager#addAssessment(Assessment)}</li>
	 * </ol>
	 * redirect to the non post version of this page.
	 *
	 * 
	 * @param form the new assessment form
	 * @param result the result used for giving feedback
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newAssessmentAssessment(
			@ModelAttribute(value = "assessment") Assessment form,
			BindingResult result, Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			if (form.getName() == null || form.getName().isEmpty()) {
				result.reject("Assessment.new.noname");
			} else {
				assessmentManager.addAssessment(form);
			}
		}
		return "redirect:.";
	}

	/**
	 * $PASTAUrl$/assessments/release/{assessmentName}/ - POST
	 * <p>
	 * Release the assessment to some students.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor: release assessment using 
	 * {@link pasta.service.AssessmentManager#releaseAssessment(String, ReleaseForm)}.
	 * redirect to $PASTAUrl$/assessments/
	 * 
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param form the release form
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "release/{assessmentName}/", method = RequestMethod.POST)
	public String releaseAssessment(
			@PathVariable("assessmentName") String assessmentName,
			@ModelAttribute(value = "assessmentRelease") ReleaseForm form,
			Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (getUser().isInstructor()) {

			if (assessmentManager.getAssessment(assessmentName) != null) {
				assessmentManager.releaseAssessment(form.getAssessmentName(),
						form);
			}
		}
		return "redirect:../../";
	}

	/**
	 * $PASTAUrl$/assessments/delete/{assessmentName}/
	 * <p>
	 * Delete the assessment
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor: delete assessment using 
	 * {@link pasta.service.AssessmentManager#removeAssessment(String)}.
	 * redirect to $PASTAUrl$/assessments/
	 * 
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "delete/{assessmentName}/")
	public String deleteAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (getUser().isInstructor()) {
			assessmentManager.removeAssessment(assessmentName);
		}
		return "redirect:../../";
	}
	
	/**
	 * $PASTAUrl$/assessments/downloadLatest/{assessmentName}/
	 * <p>
	 * Download the latest submissions for a given assessment.
	 * <p>
	 * If the user has not authenticated or is not a tutor: do nothing.
	 * <p>
	 * The http response will contain a zip file with the name $assessmentName$-latest.zip.
	 * Within that there will be a set of folders, one for each student that has made a
	 * submission with their username as the name of the folder. Within that folder is the
	 * code they submitted.
	 * 
	 * When the zip has been downloaded, it will be removed from memory.
	 * 
	 * @param assessmentName the short name (no whitespace) for the assessment
	 * @param model the model used (or not used in this case)
	 * @param response the http response that will be used to give the user the correct zip.
	 */
	@RequestMapping(value = "downloadLatest/{assessmentName}/")
	public void downloadLatest(
			@PathVariable("assessmentName") String assessmentName, Model model,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user == null || !user.isTutor()) {
			return;
		}
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=\""
				+ assessmentName + "-latest.zip\"");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outStream);
		Map<String, Map<String, AssessmentResult>> allResults = assessmentManager.getLatestResults(userManager.getStudentList());
		try {
			for(Entry<String, Map<String, AssessmentResult> > entry : allResults.entrySet()){
				if(entry.getValue() != null && 
						entry.getValue().containsKey(assessmentName) &&
						entry.getValue().get(assessmentName) != null &&
						entry.getValue().get(assessmentName).getSubmissionDate() != null){
					// add

					PASTAUtil.zip(zip, new File(ProjectProperties.getInstance()
							.getSubmissionsLocation()
							+ entry.getKey()
							+ "/assessments/"
							+ assessmentName
							+ "/"
							+ PASTAUtil.formatDate(entry.getValue().get(assessmentName).getSubmissionDate())
							+ "/submission/"), "(" + ProjectProperties.getInstance()
							.getSubmissionsLocation()
							+ ")|(assessments.*submission/)" );
					zip.closeEntry();
				}
			}
			zip.close();
			IOUtils.copy(new ByteArrayInputStream(outStream.toByteArray()),
					response.getOutputStream());
			response.flushBuffer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
//			e.printStackTrace();
		}
		catch (Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logger.error("Something really wrong happened!!!" + System.getProperty("line.separator")+sw.toString());
		}
	}
			
	/**
	 * $PASTAUrl$/assessments/stats/{assessmentName}/
	 * <p>
	 * Get the statistics for an assessment. Currently only shows a histogram of the
	 * number of submissions and a histogram of the marks.
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * <p>
	 * Build the histogram for marks and number of submissions.
	 * The number of submissions is broken up in 10 buckets.
	 * 
	 * Attributes:
	 * <table>
	 * 	<tr><td>assessment</td><td>the assessment object</td></tr>
	 * 	<tr><td>maxBreaks</td><td>the number of buckets the mark histogram is broken up in</td></tr>
	 * 	<tr><td>markDistribution</td><td>the array holding the mark distribution</td></tr>
	 * 	<tr><td>submissionDistribution</td><td>the array holding the number of submissions distribution</td></tr>
	 * 	<tr><td>unikey</td><td>the user that is currently logged in</td></tr>
	 * </table>
	 * 
	 * JSP:<ol><li>assessment/view/assessmentStats</li></ol>
	 * 
	 * @param assessmentName the short name (no whitespace) of the assessment
	 * @param model the model being used.
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/assessmentStats"
	 */
	@RequestMapping(value = "stats/{assessmentName}/")
	public String statisticsForAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		Map<String, Map<String, AssessmentResult>> allResults = assessmentManager
				.getLatestResults(userManager.getUserList());
		TreeMap<Integer, Integer> submissionDistribution = new TreeMap<Integer, Integer>();

		int maxBreaks = 10;

		int[] markDistribution = new int[maxBreaks + 1];

		for (Entry<String, Map<String, AssessmentResult>> entry : allResults
				.entrySet()) {
			int spot = 0;
			int numSubmissionsMade = 0;
			if (entry.getValue() != null
					&& entry.getValue().get(assessmentName) != null) {
				spot = ((int) (entry.getValue().get(assessmentName)
						.getPercentage() * 100 / (100 / maxBreaks)));
				numSubmissionsMade = entry.getValue().get(assessmentName)
						.getSubmissionsMade();
			}
			// mark histogram
			markDistribution[spot]++;

			// # submission distribution
			if (!submissionDistribution.containsKey(numSubmissionsMade)) {
				submissionDistribution.put(numSubmissionsMade, 0);
			}
			submissionDistribution.put(numSubmissionsMade,
					submissionDistribution.get(numSubmissionsMade) + 1);
		}

		model.addAttribute("assessment",
				assessmentManager.getAssessment(assessmentName));
		model.addAttribute("maxBreaks", maxBreaks);
		model.addAttribute("markDistribution", markDistribution);
		model.addAttribute("submissionDistribution", submissionDistribution);
		model.addAttribute("unikey", user);
		return "assessment/view/assessmentStats";
	}

}