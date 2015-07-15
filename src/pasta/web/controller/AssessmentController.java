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
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.domain.form.AssessmentReleaseForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.UpdateAssessmentForm;
import pasta.domain.upload.validate.UpdateAssessmentFormValidator;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.CompetitionManager;
import pasta.service.HandMarkingManager;
import pasta.service.ReleaseManager;
import pasta.service.SubmissionManager;
import pasta.service.UnitTestManager;
import pasta.service.UserManager;
import pasta.util.Language;
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
public class AssessmentController {

	/**
	 * Initializes the codeStyle tag mapping of file endings to 
	 * javascript tag requirements for syntax highlighting.
	 */
	public AssessmentController() {
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

	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private UnitTestManager unitTestManager;
	@Autowired
	private HandMarkingManager handMarkingManager;
	@Autowired
	private CompetitionManager competitionManager;
	@Autowired
	private SubmissionManager submissionManager;
	@Autowired
	private ReleaseManager releaseManager;
	
	@Autowired
	private UpdateAssessmentFormValidator updateValidator;

	private Map<String, String> codeStyle;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////
	
	@ModelAttribute("assessment")
	public Assessment loadAssessment(@PathVariable("assessmentId") long assessmentId) {
		return assessmentManager.getAssessment(assessmentId);
	}
	
	@ModelAttribute("updateAssessmentForm")
	public UpdateAssessmentForm loadUpdateForm(@PathVariable("assessmentId") long assessmentId) {
		return new UpdateAssessmentForm(assessmentManager.getAssessment(assessmentId));
	}
	
	@ModelAttribute("assessmentReleaseForm")
	public AssessmentReleaseForm loadAssessmentReleaseForm(@PathVariable("assessmentId") long assessmentId) {
		return new AssessmentReleaseForm(assessmentManager.getAssessment(assessmentId));
	}


	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////
	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
	public PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ASSESSMENTS //
	// ///////////////////////////////////////////////////////////////////////////


	/**
	 * $PASTAUrl$/assessments/{assessmentId}/ - GET
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
	 * @param assessmentId the id of the assessment.
	 * @param model the model used to add attributes
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/assessment"
	 */
	@RequestMapping(value = "{assessmentId}/")
	public String viewAssessment(
			@PathVariable("assessmentId") long assessmentId,
			Model model) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}

		Assessment currAssessment = assessmentManager.getAssessment(assessmentId);
		
		List<WeightedUnitTest> secretUnitTests = new LinkedList<WeightedUnitTest>();
		List<WeightedUnitTest> nonSecretUnitTests = new LinkedList<WeightedUnitTest>();
		List<WeightedUnitTest> otherUnitTests = new LinkedList<WeightedUnitTest>();
		
		// If the page is returning from a failed validation, the model 
		// will contain lists of the selected assessment modules.
		@SuppressWarnings("unchecked")
		List<WeightedUnitTest> selectedTests = (List<WeightedUnitTest>) model.asMap().get("updatedUnitTests");
		// Otherwise, just get the selected modules as stored in the database.
		if(selectedTests == null) {
			selectedTests = new LinkedList<WeightedUnitTest>(currAssessment.getAllUnitTests());
		}
		for (UnitTest test : unitTestManager.getUnitTestList()) {
			boolean contains = false;
			for (WeightedUnitTest weightedTest : selectedTests) {
				if (weightedTest.getTest().getId() == test.getId()) {
					contains = true;
					(weightedTest.isSecret() ? secretUnitTests : nonSecretUnitTests).add(weightedTest);
					break;
				}
			}

			if (!contains) {
				WeightedUnitTest weightedTest = new WeightedUnitTest();
				weightedTest.setTest(test);
				weightedTest.setWeight(0);
				weightedTest.setSecret(false);
				otherUnitTests.add(weightedTest);
			}
		}
		model.addAttribute("secretUnitTests", secretUnitTests);
		model.addAttribute("nonSecretUnitTests", nonSecretUnitTests);
		model.addAttribute("otherUnitTests", otherUnitTests);

		List<WeightedHandMarking> otherHandMarking = new LinkedList<WeightedHandMarking>();

		@SuppressWarnings("unchecked")
		List<WeightedHandMarking> selectedHandMarking = (List<WeightedHandMarking>) model.asMap().get("updatedHandMarking");
		if(selectedHandMarking == null) {
			selectedHandMarking = new LinkedList<WeightedHandMarking>(currAssessment.getHandMarking());
		}
		for (HandMarking test : handMarkingManager.getHandMarkingList()) {
			boolean contains = false;
			for (WeightedHandMarking weightedHandMarking : selectedHandMarking) {
				if (weightedHandMarking.getHandMarking().getId() == test.getId()) {
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
		model.addAttribute("handMarkingTemplates", selectedHandMarking);
		model.addAttribute("otherHandMarking", otherHandMarking);

		List<WeightedCompetition> otherCompetitions = new LinkedList<WeightedCompetition>();

		@SuppressWarnings("unchecked")
		List<WeightedCompetition> selectedCompetitions = (List<WeightedCompetition>) model.asMap().get("updatedCompetitions");
		if(selectedCompetitions == null) {
			selectedCompetitions = new LinkedList<WeightedCompetition>(currAssessment.getCompetitions());
		}
		for (Competition comp : competitionManager.getCompetitionList()) {
			boolean contains = false;
			for (WeightedCompetition weightedComp : selectedCompetitions) {
				if (weightedComp.getCompetition().getId() == comp.getId()) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				WeightedCompetition weightedComp = new WeightedCompetition();
				weightedComp.setCompetition(comp);
				weightedComp.setWeight(0);
				otherCompetitions.add(weightedComp);
			}
		}
		model.addAttribute("competitions", selectedCompetitions);
		model.addAttribute("otherCompetitions", otherCompetitions);
		
		model.addAttribute("tutorialByStream", userManager.getTutorialByStream());
		model.addAttribute("allLanguages", Language.values());
		
		model.addAttribute("unikey", user);
		return "assessment/view/assessment";
	}
	
	/**
	 * $PASTAUrl$/assessments/{assessmentId}/ - POST
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
	 * @param assessmentId the id of the assessment
	 * @param form the form for updating the assessment
	 * @param result the binding result, used for feedback
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:."
	 */
	@RequestMapping(value = "{assessmentId}/", method = RequestMethod.POST)
	public String updateAssessment(
			@PathVariable("assessmentId") long assessmentId,
			@Valid @ModelAttribute(value = "updateAssessmentForm") UpdateAssessmentForm form, BindingResult result,
			@ModelAttribute(value = "assessment") Assessment assessment,
			RedirectAttributes attr, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		
		updateValidator.validate(form, result);
		if(result.hasErrors()) {
			attr.addFlashAttribute("updateAssessmentForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.updateAssessmentForm", result);
			
			// Reload the unit tests, hand marking and competitions to that the
			// page can properly re-display selected tests
			for(WeightedUnitTest test : form.getNewUnitTests()) {
				test.setTest(unitTestManager.getUnitTest(test.getTest().getId()));
				test.setSecret(false);
			}
			for(WeightedUnitTest test : form.getNewSecretUnitTests()) {
				test.setTest(unitTestManager.getUnitTest(test.getTest().getId()));
				test.setSecret(true);
			}
			attr.addFlashAttribute("updatedUnitTests", form.getAllUnitTests());
			
			for(WeightedHandMarking template : form.getNewHandMarking()) {
				template.setHandMarking(handMarkingManager.getHandMarking(template.getHandMarking().getId()));
			}
			attr.addFlashAttribute("updatedHandMarking", form.getNewHandMarking());
			
			for(WeightedCompetition comp : form.getNewCompetitions()) {
				comp.setCompetition(competitionManager.getCompetition(comp.getCompetition().getId()));
			}
			attr.addFlashAttribute("updatedCompetitions", form.getNewCompetitions());
			return "redirect:.";
		}
		
		if (user.isInstructor()) {
			assessmentManager.updateAssessment(assessment, form);
		}
		return "redirect:.";
	}

	/**
	 * $PASTAUrl$/assessments/{assessmentId}/run/
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
	 * @param assessmentId the id of the assessment
	 * @param request the http request, used for redirection
	 * @return "redirect:/login/" or "redirect:/home/" or redirect to referrer
	 */
	@RequestMapping(value = "{assessmentId}/run/")
	public String runAssessment(
			@PathVariable("assessmentId") long assessmentId,
			HttpServletRequest request) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (user.isInstructor()) {
			submissionManager.runAssessment(assessmentManager.getAssessment(assessmentId), userManager.getUserList());
		}
		return "redirect:" + request.getHeader("Referer");
	}

//	/**
//	 * $PASTAUrl$/assessments/release/{assessmentId}/ - POST
//	 * <p>
//	 * Release the assessment to some students.
//	 * <p>
//	 * If the user has not authenticated: redirect to login.
//	 * 
//	 * If the user is not a tutor: redirect to home
//	 * 
//	 * If the user is an instructor: release assessment using 
//	 * {@link pasta.service.AssessmentManager#releaseAssessment(String, AssessmentReleaseForm)}.
//	 * redirect to $PASTAUrl$/assessments/
//	 * 
//	 * @param assessmentId the id of the assessment
//	 * @param form the release form
//	 * @param model the model used
//	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
//	 */
//	@RequestMapping(value = "release/{assessmentId}/", method = RequestMethod.POST)
//	public String releaseAssessment(
//			@PathVariable("assessmentId") long assessmentId,
//			@ModelAttribute(value = "assessment") Assessment assessment,
//			@ModelAttribute(value = "assessmentReleaseForm") AssessmentReleaseForm form,
//			Model model) {
//
//		PASTAUser user = getUser();
//		if (user == null) {
//			return "redirect:/login/";
//		}
//		if (!user.isTutor()) {
//			return "redirect:/home/";
//		}
//		if (getUser().isInstructor()) {
//
//			if (assessmentManager.getAssessment(assessmentId) != null) {
//				assessmentManager.releaseAssessment(assessment, form);
//			}
//		}
//		return "redirect:../../";
//	}

	/**
	 * $PASTAUrl$/assessments/delete/{assessmentId}/
	 * <p>
	 * Delete the assessment
	 * <p>
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an instructor: delete assessment using 
	 * {@link pasta.service.AssessmentManager#removeAssessment(long)}.
	 * redirect to $PASTAUrl$/assessments/
	 * 
	 * @param assessmentId the id of the assessment
	 * @param model the model used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../../"
	 */
	@RequestMapping(value = "delete/{assessmentId}/")
	public String deleteAssessment(
			@PathVariable("assessmentId") long assessmentId, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if (getUser().isInstructor()) {
			assessmentManager.removeAssessment(assessmentId);
		}
		return "redirect:../../";
	}
	
	/**
	 * $PASTAUrl$/assessments/downloadLatest/{assessmentId}/
	 * <p>
	 * Download the latest submissions for a given assessment.
	 * <p>
	 * If the user has not authenticated or is not a tutor: do nothing.
	 * <p>
	 * The http response will contain a zip file with the name $assessmentId$-latest.zip.
	 * Within that there will be a set of folders, one for each student that has made a
	 * submission with their username as the name of the folder. Within that folder is the
	 * code they submitted.
	 * 
	 * When the zip has been downloaded, it will be removed from memory.
	 * 
	 * @param assessmentId the id for the assessment
	 * @param model the model used (or not used in this case)
	 * @param response the http response that will be used to give the user the correct zip.
	 */
	@RequestMapping(value = "downloadLatest/{assessmentId}/")
	public void downloadLatest(
			@PathVariable("assessmentId") long assessmentId, Model model,
			HttpServletResponse response) {
		PASTAUser user = getUser();
		if (user == null || !user.isTutor()) {
			return;
		}
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment;filename=\""
				+ assessment.getFileAppropriateName() + "-latest.zip\"");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outStream);
		Map<PASTAUser, Map<Long, AssessmentResult>> allResults = assessmentManager.getLatestResults(userManager.getStudentList());
		try {
			for(Entry<PASTAUser, Map<Long, AssessmentResult> > entry : allResults.entrySet()){
				if(entry.getValue() != null && 
						entry.getValue().containsKey(assessmentId) &&
						entry.getValue().get(assessmentId) != null &&
						entry.getValue().get(assessmentId).getSubmissionDate() != null){
					// add

					PASTAUtil.zip(zip, new File(ProjectProperties.getInstance()
							.getSubmissionsLocation()
							+ entry.getKey().getUsername()
							+ "/assessments/"
							+ assessmentId
							+ "/"
							+ PASTAUtil.formatDate(entry.getValue().get(assessmentId).getSubmissionDate())
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
	 * $PASTAUrl$/assessments/stats/{assessmentId}/
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
	 * @param assessmentId the id of the assessment
	 * @param model the model being used.
	 * @return "redirect:/login/" or "redirect:/home/" or "assessment/view/assessmentStats"
	 */
	@RequestMapping(value = "stats/{assessmentId}/")
	public String statisticsForAssessment(
			@PathVariable("assessmentId") long assessmentId, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		Map<PASTAUser, Map<Long, AssessmentResult>> allResults = assessmentManager
				.getLatestResults(userManager.getUserList());
		TreeMap<Integer, Integer> submissionDistribution = new TreeMap<Integer, Integer>();

		int maxBreaks = 10;

		int[] markDistribution = new int[maxBreaks + 1];

		for (Entry<PASTAUser, Map<Long, AssessmentResult>> entry : allResults
				.entrySet()) {
			int spot = 0;
			int numSubmissionsMade = 0;
			if (entry.getValue() != null
					&& entry.getValue().get(assessmentId) != null) {
				spot = ((int) (entry.getValue().get(assessmentId)
						.getPercentage() * 100 / (100 / maxBreaks)));
				numSubmissionsMade = entry.getValue().get(assessmentId)
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

		model.addAttribute("maxBreaks", maxBreaks);
		model.addAttribute("markDistribution", markDistribution);
		model.addAttribute("submissionDistribution", submissionDistribution);
		model.addAttribute("unikey", user);
		return "assessment/view/assessmentStats";
	}

}
