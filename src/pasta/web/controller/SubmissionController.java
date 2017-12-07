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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pasta.archive.ArchiveEntry;
import pasta.archive.InvalidRebuildOptionsException;
import pasta.archive.RebuildOptions;
import pasta.domain.FileTreeNode;
import pasta.domain.UserPermissionLevel;
import pasta.domain.form.NewUnitTestForm;
import pasta.domain.form.Submission;
import pasta.domain.form.validate.SubmissionValidator;
import pasta.domain.ratings.AssessmentRating;
import pasta.domain.ratings.RatingForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.BlackBoxOptions;
import pasta.domain.template.BlackBoxTestCase;
import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.repository.AssessmentDAO;
import pasta.repository.BaseDAO;
import pasta.repository.HandMarkingDAO;
import pasta.repository.RatingDAO;
import pasta.repository.ResultDAO;
import pasta.repository.UnitTestDAO;
import pasta.repository.UserDAO;
import pasta.scheduler.AssessmentJob;
import pasta.scheduler.ExecutionEstimator;
import pasta.scheduler.ExecutionScheduler;
import pasta.service.ArchiveManager;
import pasta.service.AssessmentManager;
import pasta.service.ExecutionManager;
import pasta.service.GroupManager;
import pasta.service.PASTAOptions;
import pasta.service.RatingManager;
import pasta.service.ResultManager;
import pasta.service.SubmissionManager;
import pasta.service.UserManager;
import pasta.service.ValidationManager;
import pasta.service.validation.SubmissionValidationResult;
import pasta.util.PASTAUtil;
import pasta.web.WebUtils;

/**
 * Controller class for the submission (pretty much other, I just dump
 * everything that didn't fit in the other controllers in here) functions.
 * <p>
 * Handles mappings of $PASTAUrl$/...
 * <p>
 * Both students and teaching staff can access this url.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
@Controller
@RequestMapping("/")
public class SubmissionController {

	public SubmissionController() {
	}

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private AssessmentDAO assessmentDAO;
	
	@Autowired
	private SubmissionManager manager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentManager assessmentManager;
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private RatingManager ratingManager;
	@Autowired
	private GroupManager groupManager;
	@Autowired
	private ExecutionScheduler scheduler;
	@Autowired
	private ValidationManager validationManager;
	
	@Autowired
	private SubmissionValidator submissionValidator;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newUnitTestModel")
	public NewUnitTestForm returnNewUnitTestModel() {
		return new NewUnitTestForm();
	}

	@ModelAttribute("submission")
	public Submission returnSubmissionModel() {
		return new Submission();
	}

	@ModelAttribute("assessmentResult")
	public AssessmentResult returnAssessmentResultModel() {
		return new AssessmentResult();
	}
	
	@ModelAttribute("ratingForms")
	public Map<Long, RatingForm> loadRatingForms() {
		Map<Long, RatingForm> forms = new HashMap<Long, RatingForm>();
		
		PASTAUser user = WebUtils.getUser();
		if(user == null) {
			return null;
		}
		
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			AssessmentRating rating = ratingManager.getRating(assessment, user);
			if(rating == null) {
				rating = new AssessmentRating(assessment, user);
			}
			forms.put(assessment.getId(), new RatingForm(rating));
		}
		
		return forms;
	}
	
	@ModelAttribute("ratingForm")
	public RatingForm loadRatingForm() {
		return new RatingForm();
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	@Autowired
	private ExecutionManager execManager;
	@RequestMapping(value = "run/")
	@ResponseBody
	public String doTests(@ModelAttribute("user")PASTAUser user, Model model, HttpServletRequest request, HttpSession session) {
		execManager.fixWaitingJobs();
		execManager.executeRemainingAssessmentJobs();
		return "Executing";
	}
	
	@Autowired
	@Qualifier("baseDAO")
	private BaseDAO baseDAO;
	
	@Autowired
	private ArchiveManager archiveManager;
	
	@RequestMapping(value = "serial/1/")
	@ResponseBody
	public String doSerial1(@ModelAttribute("user")PASTAUser user, Model model, HttpServletRequest request, HttpSession session) {
		
		archiveManager.test1();
		return "<h1>OKAY 1</h1>";
	}
		
	@RequestMapping(value = "serial/2/")
	@ResponseBody
	public String doSerial2(@ModelAttribute("user")PASTAUser user, Model model, HttpServletRequest request, HttpSession session) {
		
		archiveManager.test2();
		
//		logger.info("Saving extra rating...");
//		AssessmentRating assessmentRating = new AssessmentRating(assessmentDAO.getAssessment(1), user);
//		assessmentRating.setRating(9);
//		baseDAO.save(assessmentRating);
		
		/*
		Assessment ass = new Assessment();
		ass.setName("Testing");
		ass.setMarks(10);
		ass.setDueDate(new Date());
		
		AssessmentDAO dao = ProjectProperties.getInstance().getAssessmentDAO();
		
		logger.info("---- SAVING Assessment");
		dao.saveOrUpdate(ass);
		Long id = ass.getId();
		ass = dao.getAssessment(id);
		logger.info("---- Assessment: " + ass.getName() + " (" + ass.getMarks() + ")");
		
		UserDAO udao = ProjectProperties.getInstance().getUserDAO();
		PASTAUser me = udao.getUser("jstretton");
		
		logger.info("---- ADDING UNIT TEST");
		WeightedUnitTest wut = new WeightedUnitTest();
		UnitTest ut = ProjectProperties.getInstance().getUnitTestDAO().getUnitTest(1);
		wut.setTest(ut);
		wut.setWeight(1);
		ass.addUnitTest(wut);
		dao.saveOrUpdate(ass);
		ass = dao.getAssessment(id);
		logger.info("---- Assessment unit tests: " + ass.getAllUnitTests().size());
		
		logger.info("---- ADDING HAND MARKING");
		WeightedHandMarking whm = new WeightedHandMarking();
		HandMarking hm = ProjectProperties.getInstance().getHandMarkingDAO().getHandMarking(1);
		whm.setHandMarking(hm);
		whm.setWeight(1);
		ass.addHandMarking(whm);
		dao.saveOrUpdate(ass);
		ass = dao.getAssessment(id);
		logger.info("---- Assessment hand marking: " + ass.getHandMarking().size());
		
		ResultDAO rdao = ProjectProperties.getInstance().getResultDAO();
		
		logger.info("---- SETTING UP MARKS");
		AssessmentResult result = new AssessmentResult();
		result.setAssessment(ass);
		result.setUser(me);
		result.setSubmittedBy(me);
		result.setSubmissionDate(new Date());
		
		UnitTestResult utr = new UnitTestResult();
		logger.info(" ================================ WUT ID? " + (wut.getId()));
		wut = ProjectProperties.getInstance().getUnitTestDAO().getWeightedUnitTest(wut.getId());
		utr.setWeightedUnitTest(wut);
		utr.setCompileErrors("COMPILE ERRORS!!!");
		result.addUnitTest(utr);
		
		HandMarkingResult hmr = new HandMarkingResult();
		hmr.setWeightedHandMarking(whm);
		Map<Long,Long> resultMap = new HashMap<>();
		resultMap.put(4L, 2L);
		resultMap.put(5L, 3L);
		resultMap.put(6L, 3L);
		hmr.setResult(resultMap);
		result.addHandMarkingResult(hmr);
		
		rdao.saveOrUpdate(result);
		Long rId = result.getId();
		result = rdao.getAssessmentResult(rId);
		logger.info("---- Assessment results: " + result.getMarks());
		*/
		
		/*
		PASTAUser newuser = new PASTAUser();
		newuser.setActive(true);
		newuser.setPermissionLevel(UserPermissionLevel.STUDENT);
		newuser.setStream("");
		newuser.setTutorial("");
		newuser.setUsername("newuser");
		udao.save(newuser);
		*/
		
		/*
		logger.info("---- SAVING Rating");
		AssessmentRating rating = new AssessmentRating(ass, me);
		rating.setComment("LOL this was TERRIBLE");
		rating.setRating(9001);
		ratingDAO.saveOrUpdate(rating);
		rating = ratingDAO.getRating(ass, me);
		logger.info("---- Rating: " + rating.getComment() + " (" + rating.getRating() + ")");
		
		WeightedUnitTest wut1 = ass.getAllUnitTests().iterator().next();
		WeightedUnitTest wut2 = result.getUnitTests().iterator().next().getWeightedUnitTest();
		
		logger.info(" ================================ WUT Same as each other? " + (wut1 == wut2));
		logger.info(" ================================ WUT ass same as wut? " + (wut1 == wut));
		logger.info(" ================================ WUT result same as wut? " + (wut2 == wut));
		
		WeightedHandMarking whm1 = ass.getHandMarking().iterator().next();
		WeightedHandMarking whm2 = result.getHandMarkingResults().iterator().next().getWeightedHandMarking();
		
		logger.info(" ================================ WHM Same as each other? " + (whm1 == whm2));
		logger.info(" ================================ WHM ass same as wut? " + (whm1 == whm));
		logger.info(" ================================ WHM result same as wut? " + (whm2 == whm));
		
		logger.info("---- DELETING Assessment");
		dao.delete(ass);
		ass = dao.getAssessment(id);
		logger.info("---- Assessment: " + ass);
		
		result = rdao.getAssessmentResult(rId);
		logger.info("---- Assessment results after delete: " + result);
		*/
		
		/*
		File f = new File(ProjectProperties.getInstance().getProjectLocation(), "test.ser");
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			FileOutputStream fileOut = new FileOutputStream(f);
			out = new ObjectOutputStream(fileOut);
			Assessment ass = assessmentManager.getAssessment(1);
			
			ArchiveItem<Assessment> item = new ArchiveItem<>(ass);
			
			item.archiveTo(out);
			out.close();
			
			FileInputStream fileIn = new FileInputStream(f);
			in = new ObjectInputStream(fileIn);
			ArchiveItem<Assessment> item2 = (ArchiveItem<Assessment>) ArchiveItem.readFrom(in);
			in.close();
			
			Assessment ass2 = (Assessment) item2.getData();
//			item2.clearDataId();
			logger.info("Part of this instance?: " + item2.isFromThisInstance());
			
			RebuildOptions options = new RebuildOptions();
			Assessment clone;
			try {
				clone = ass2.rebuild(options);
				clone.setName(clone.getName() + " (copy)");
				logger.info("Rebuilt " + clone);
				logger.info("Rebuilt id " + clone.getId());
				ProjectProperties.getInstance().getAssessmentDAO().deepSaveOrUpdate(clone);
			} catch (InvalidRebuildOptionsException e) {
				logger.error("Error rebuilding assessment", e);
			}
			
			
//			FileOutputStream fileOut = new FileOutputStream(f);
//			out = new ObjectOutputStream(fileOut);
//			Assessment ass = assessmentManager.getAssessment(1);
//			ArchiveItem item = new ArchiveItem(ass);
//			
//			logger.info("First item, part of this instance? " + item.isFromThisInstance());
//			
//			item.archiveTo(out);
//			out.close();
//			
//			FileInputStream fileIn = new FileInputStream(f);
//			in = new ObjectInputStream(fileIn);
//			ArchiveItem item2 = ArchiveItem.readFrom(in);
//			in.close();
//			
//			Assessment ass2 = (Assessment) item2.getData();
//			
//			logger.info("Second item, description: " + ass2.getDescription());
//			logger.info("Part of this instance?: " + item2.isFromThisInstance());
//			logger.info("Second item ID before resetting: " + ass2.getId());
//			logger.info("First item ID before resetting: " + ass.getId());
//			item2.clearDataId();
//			logger.info("Second item ID after resetting: " + ass2.getId());
//			logger.info("First item ID after resetting: " + ass.getId());
//			
//			
//			
//			Set<WeightedCompetition> c1 = ass.getCompetitions();
//			logger.info("competitions collection 1: " + c1.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(c1)));
//			Set<WeightedCompetition> c2 = ass2.getCompetitions();
//			logger.info("competitions collection 2: " + c2.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(c2)));
//			
////			WeightedUnitTest t1 = ass.getAllUnitTests().iterator().next();
////			logger.info("unit test 1: " + t1.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(t1)));
////			WeightedUnitTest t2 = ass2.getAllUnitTests().iterator().next();
////			logger.info("unit test 2: " + t2.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(t2)));
//			
//			logger.info("Unit tests 1 length: " + ass.getAllUnitTests().size());
//			logger.info("Unit tests 2 length: " + ass2.getAllUnitTests().size());
//			
//			ass2.setName(ass.getName() + " (Copy)");
//			
//			ProjectProperties.getInstance().getAssessmentDAO().saveArchivedItem(ass2);
			
			return "<p>" + ass.getDescription() + "</p>";
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				out.close();
			} catch (Exception e) {}
			try {
				in.close();
			} catch (Exception e) {}
		}
		*/
		return "<h1>OKAY 2</h1>";
	}
	
	// ///////////////////////////////////////////////////////////////////////////
	// HOME //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/home/
	 * <p>
	 * The home screen.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBTUES:
	 * <table>
	 * <tr>
	 * <td>assessments</td>
	 * <td>map of all assessments by category (key of category: String, value of
	 * list of {@link pasta.domain.template.Assessment}</td>
	 * </tr>
	 * <tr>
	 * <td>results</td>
	 * <td>The results as a map (key of id of assessment: Long, value of
	 * {@link pasta.domain.result.AssessmentResult}</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/tutorHome - if the user is a tutor</li>
	 * <li>user/studentHome - if the user is a student</li>
	 * </ul>
	 * 
	 * @param model
	 *            the model being used
	 * @param session
	 *            the http session that may hold the binding result information.
	 * @return "redirect:/login/" or "user/tutorHome" or "user/studentHome"
	 */
	@RequestMapping(value = "home/")
	public String home(@ModelAttribute("user")PASTAUser user, Model model, HttpServletRequest request, HttpSession session) {
		WebUtils.ensureLoggedIn(request);

		model.addAttribute("results", resultManager.getLatestResultsIncludingGroups(user));
		
		Map<String, Set<Assessment>> allAssessments = assessmentManager.getAllAssessmentsByCategory(user.isTutor());
		Iterator<String> itCategories = allAssessments.keySet().iterator();
		while(itCategories.hasNext()) {
			String category = itCategories.next();
			Iterator<Assessment> itAssessments = allAssessments.get(category).iterator();
			while(itAssessments.hasNext()) {
				Assessment a = itAssessments.next();
				if(!a.isReleasedTo(user)) {
					itAssessments.remove();
				}
			}
			if(allAssessments.get(category).isEmpty()) {
				itCategories.remove();
			}
		}
		model.addAttribute("assessments", allAssessments);
		
		
		Map<Long, String> dueDates = new HashMap<Long, String>();
		Map<Long, Boolean> hasExtension = new HashMap<>();
		Map<Long, Boolean> closed = new HashMap<>();
		Map<Long, Boolean> hasGroupWork = new HashMap<>();
		Map<Long, Boolean> allGroupWork = new HashMap<>();
		for(Assessment assessment : assessmentManager.getAssessmentList()) {
			Date due = userManager.getDueDate(user, assessment);
			String formatted = PASTAUtil.formatDateReadable(due);
			dueDates.put(assessment.getId(), formatted);
			Date extension = userManager.getExtension(user, assessment);
			hasExtension.put(assessment.getId(), extension != null);
			closed.put(assessment.getId(), assessment.isClosedFor(user, extension));
			boolean userHasGroup = groupManager.getGroup(user, assessment) != null;
			boolean assessmentHasGroupWork = userHasGroup && assessmentManager.hasGroupWork(assessment);
			hasGroupWork.put(assessment.getId(), assessmentHasGroupWork);
			allGroupWork.put(assessment.getId(), assessmentHasGroupWork && assessmentManager.isAllGroupWork(assessment));
		}
		model.addAttribute("dueDates", dueDates);
		model.addAttribute("hasExtension", hasExtension);
		model.addAttribute("closed", closed);
		model.addAttribute("hasGroupWork", hasGroupWork);
		model.addAttribute("allGroupWork", allGroupWork);
		
		model.addAttribute("individualDeclaration", PASTAOptions.instance().get("submission.individual.text"));
		model.addAttribute("groupDeclaration", PASTAOptions.instance().get("submission.group.text"));
		
		if (user.isTutor()) {
			return "user/tutorHome";
		} else {
			return "user/studentHome";
		}
	}

	/**
	 * $PASTAUrl/home/ - POST
	 * <p>
	 * Submit an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * FAIL CONDITIONS:
	 * <table>
	 * <tr>
	 * <td>Submission.NoFile</td>
	 * <td>The form doesn't contain a file</td>
	 * <tr>
	 * <td>Submission.AfterClosingDate</td>
	 * <td>The submission is past the due date</td>
	 * </tr>
	 * <tr>
	 * <td>Submission.NoAttempts</td>
	 * <td>There are no attempts left to this user</td>
	 * </tr>
	 * </table>
	 * 
	 * If the submission is validated correctly and doesn't encounter any of the
	 * fail conditions, it's submitted using
	 * {@link pasta.service.SubmissionManager#submit(String, Submission)}
	 * 
	 * Then it's redirected to $PASTAUrl$/mirror/ to clear the form submission
	 * buffer such that pressing reload has no chance of resubmitting your form.
	 * 
	 * @param form
	 *            the submission form holding the code
	 * @param result
	 *            the binding result used for feedback
	 * @param model
	 *            the model being used
	 * @param session
	 *            the http session used for allowing passing the binding result
	 *            along while using $PASTAUrl$/mirror/ to redirect and such that
	 *            you will not resubmit the form if you refresh the page
	 * @return "redirect:/login/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "home/", method = RequestMethod.POST)
	public String submitAssessment(@ModelAttribute("user")PASTAUser user, @ModelAttribute(value = "submission") Submission form,
			BindingResult result, Model model, RedirectAttributes attr, HttpSession session) {
		return doSubmitAssessment(user, null, form, result, model, attr, session);
	}
	
	private String doSubmitAssessment(@ModelAttribute("user")PASTAUser user, String submitForUsername, Submission form, BindingResult result, Model model, RedirectAttributes attr, HttpSession session) {
		form.setSubmittingUser(user);
		
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		form.setSubmissionDate(now.getTime());
		
		PASTAUser forUser = user;
		if(submitForUsername != null) {
			if(!user.isTutor()) {
				return "redirect:/home/";
			}
			forUser = userManager.getUser(submitForUsername);
			if(forUser == null) {
				return "redirect:/home/";
			}
		}
		
		if(form.isGroupSubmission()) {
			PASTAGroup group = groupManager.getGroup(forUser, form.getAssessment());
			if(group == null) {
				result.reject("NoGroup");
			} else {
				forUser = group;
			}
		}
		
		submissionValidator.validate(forUser, form, result);
		if(result.hasErrors()) {
			attr.addFlashAttribute("submission", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.submission", result);
			return "redirect:.";
		}
		
		Assessment assessment = assessmentDAO.getAssessment(form.getAssessment());
		
		SubmissionValidationResult validationResult = validationManager.validate(forUser, assessment, form);
		session.setAttribute("validationResults", validationResult);
		if(validationResult != null && validationResult.hasErrors()) {
			return "redirect:/mirror/";
		}
		
		logger.info(assessment.getName()
				+ " submitted for " + forUser.getUsername() + " by " + user.getUsername());
		manager.submit(forUser, form);
		
		return "redirect:/mirror/";
	}
	
	@RequestMapping("**/home/clearValidationResults/") 
	@ResponseBody
	public String clearValidationResults(@RequestParam(value="part", required=false) String part, HttpSession session) {
		SubmissionValidationResult result = (SubmissionValidationResult) session.getAttribute("validationResults");
		if(result == null) {
			return "SUCCESS";
		}
		boolean success = false;
		if(part.equals("error")) {
			result.setErrors(null);
			success = true;
		} else if(part.equals("feedback")) {
			result.setFeedback(null);
			success = true;
		} 
		if(part == null || (!result.hasErrors() && !result.hasFeedback())) {
			session.removeAttribute("validationResults");
			success = true;
		}
		return success ? "SUCCESS" : "";
	}
	
	@RequestMapping("checkJobQueue/{assessmentId}/") 
	@ResponseBody
	public String checkJobQueue(@ModelAttribute("user")PASTAUser user, @PathVariable("assessmentId") long assessmentId) {
		return doCheckJobQueue(user, assessmentId);
	}
	
	@RequestMapping("student/{studentUsername}/checkJobQueue/{assessmentId}/") 
	@ResponseBody
	public String checkJobQueue(@PathVariable("assessmentId") long assessmentId, 
			@PathVariable("studentUsername") String username) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser forUser = userManager.getUser(username);
		if(forUser == null) {
			return "error";
		}
		return doCheckJobQueue(forUser, assessmentId);
	}

	private String doCheckJobQueue(PASTAUser forUser, long assessmentId) {
		List<AssessmentJob> jobs = scheduler.getAssessmentQueue();
		if(jobs == null || jobs.isEmpty()) {
			return "";
		}
		PASTAGroup userGroup = groupManager.getGroup(forUser, assessmentId);
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();
		
		long totalTime = 0;
		ArrayNode positions = mapper.createArrayNode();
		for(int i = 0; i < jobs.size(); i++) {
			AssessmentJob job = jobs.get(i);
			if(i == 0) {
				result.put("current", job.getId());
			}
			totalTime += ExecutionEstimator.estimateTime(job);
			if(job.getAssessmentId() == assessmentId
					&& (job.getUser().equals(forUser) ||
							(userGroup != null && job.getUser().equals(userGroup)))) {
				ObjectNode positionNode = mapper.createObjectNode();
				positionNode.put("position", i+1);
				positionNode.put("estimatedComplete", totalTime);
				positionNode.put("running", job.isRunning());
				positions.add(positionNode);
			}
		}
		
		result.set("positions", positions);
		return result.toString();
	}
	
	@RequestMapping("utResults/{assessmentId}/") 
	public String loadUtResults(Model model,
			@ModelAttribute("user")PASTAUser user, 
			@PathVariable("assessmentId") long assessmentId,
			@RequestParam("detailsLink") String detailsLink,
			@RequestParam("summary") boolean summary,
			@RequestParam("separateGroup") boolean separateGroup) {
		return doLoadUtResults(model, user, assessmentId, detailsLink, summary, separateGroup);
	}
	
	@RequestMapping("student/{studentUsername}/utResults/{assessmentId}/") 
	public String loadUtResults(Model model,
			@PathVariable("assessmentId") long assessmentId, 
			@PathVariable("studentUsername") String username,
			@RequestParam("detailsLink") String detailsLink,
			@RequestParam("summary") boolean summary,
			@RequestParam("separateGroup") boolean separateGroup) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser forUser = userManager.getUser(username);
		if(forUser == null) {
			return "error";
		}
		return doLoadUtResults(model, forUser, assessmentId, detailsLink, summary, separateGroup);
	}
	
	private String doLoadUtResults(Model model, PASTAUser forUser, long assessmentId, String detailsLink, boolean summary, boolean separateGroup) {
		model.addAttribute("results", resultManager.getLatestResultIncludingGroup(forUser, assessmentId));
		model.addAttribute("detailsLink", detailsLink);
		model.addAttribute("summary", summary);
		model.addAttribute("separateGroup", separateGroup);
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		Date extension = userManager.getExtension(forUser, assessment);
		model.addAttribute("closedAssessment", assessment.isClosedFor(forUser, extension));
		return "assessment/results/resultsPartial";
	}
	
	@RequestMapping("latestMark/{assessmentId}/") 
	@ResponseBody
	public String checkLatestMark(@ModelAttribute("user")PASTAUser user, @PathVariable("assessmentId") long assessmentId) {
		return getLatestMark(user, assessmentId, !user.isTutor());
	}
	
	@RequestMapping("student/{studentUsername}/latestMark/{assessmentId}/") 
	@ResponseBody
	public String checkLatestMark(@PathVariable("assessmentId") long assessmentId, 
			@PathVariable("studentUsername") String username) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		PASTAUser forUser = userManager.getUser(username);
		if(forUser == null) {
			return "error";
		}
		return getLatestMark(forUser, assessmentId, true);
	}
	
	private String getLatestMark(PASTAUser user, long assessmentId, boolean hide) {
		AssessmentResult results = resultManager.getLatestResultIncludingGroup(user, assessmentId);
		if(results == null) {
			return "0";
		}
		if(hide && (!results.isFinishedHandMarking() || 
				(results.getAssessment().isClosed() && !results.getAssessment().getSecretUnitTests().isEmpty()))) {
			return "???";
		}
		double marks = results.getMarks();
		if(marks % 1 == 0) {
			return String.valueOf((int)marks);
		}
		return String.valueOf(Math.round(results.getMarks() * 1000.0) / 1000.0);
	}

	/**
	 * $PASTAUrl$/info/{assessmentId}/
	 * <p>
	 * View the details and history for an assessment.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * <tr>
	 * <td>assessment</td>
	 * <td>{@link pasta.domain.template.Assessment} the assessment</td>
	 * </tr>
	 * <tr>
	 * <td>history</td>
	 * <td>Collection of {@link pasta.domain.result.AssessmentResult} for all of
	 * the submissions</td>
	 * </tr>
	 * <tr>
	 * <td>nodeList</td>
	 * <td>Map of {@link pasta.domain.FileTreeNode} with the key as the date of
	 * the submission and the value being the root node of the code that was
	 * submitted.</td>
	 * </tr>
	 * </table>
	 * 
	 * JSP:
	 * <ul>
	 * <li>user/viewAssessment</li>
	 * </ul>
	 * 
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "user/viewAssessment"
	 */
	@RequestMapping(value = "info/{assessmentId}/")
	public String viewAssessmentInfo(@ModelAttribute("user") PASTAUser user, @PathVariable("assessmentId") long assessmentId, Model model) {
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(assessment == null) {
			return "redirect:/home/";
		}

		model.addAttribute("assessment", assessment);
		model.addAttribute("closed", assessment.isClosedFor(user, userManager.getExtension(user, assessment)));
		model.addAttribute("extension", userManager.getExtension(user, assessment));
		
		List<AssessmentResult> history = resultManager.getAssessmentHistory(user, assessmentId);
		model.addAttribute("history", history);
		
		Map<Long, String> lateString = new HashMap<Long, String>();
		for(AssessmentResult result : history) {
			Date due = result.getAssessment().getDueDate();
			Date submitted = result.getSubmissionDate();
			if(submitted.after(due)) {
				lateString.put(result.getId(), " (" + PASTAUtil.dateDiff(due, submitted) + " late)");
			}
		}
		model.addAttribute("lateString", lateString);
		
		Map<String, FileTreeNode> nodes = PASTAUtil.generateFileTree(user, assessmentId);
		PASTAUser group = groupManager.getGroup(user, assessmentId);
		if(group != null) {
			nodes.putAll(PASTAUtil.generateFileTree(group, assessmentId));
		}
		model.addAttribute("nodeList", nodes);

		return "user/viewAssessment";
	}

	// ///////////////////////////////////////////////////////////////////////////
	// VIEW //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/student/{studentUsername}/info/{assessmentId}/updateComment/ - POST
	 * <p>
	 * Update the comment for a given user for a given assessment for a given
	 * submission using
	 * {@link pasta.service.HandMarkingManager#saveComment(String, long, String, String)}
	 * 
	 * @param newComment
	 *            the new comment
	 * @param assessmentDate
	 *            the assessment date (format: yyyy-MM-dd'T'HH-mm-ss)
	 * @param studentUsername
	 *            the name of the user
	 * @param assessmentId
	 *            the id of the assessment
	 * @param model
	 *            the model being used
	 * @return "redirect:/login/" or "redirect:/home/" or "redirect:../"
	 */
	@RequestMapping(value = "student/{studentUsername}/info/{assessmentId}/updateComment/", method = RequestMethod.POST)
	public String updateComment(@RequestParam("newComment") String newComment,
			@RequestParam("resultId") long resultId, Model model) {
		WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		resultManager.updateComment(resultId, newComment);
		return "redirect:../";
	}

	/**
	 * $PASTAUrl$/student/{studentUsername}/home/
	 * <p>
	 * Submit an assessment for another user.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home.
	 * 
	 * FAIL CONDITIONS:
	 * <table>
	 * <tr>
	 * <td>Submission.NoFile</td>
	 * <td>The form doesn't contain a file</td>
	 * </tr>
	 * </table>
	 * 
	 * If the submission is validated correctly and doesn't encounter any of the
	 * fail conditions, it's submitted using
	 * {@link pasta.service.SubmissionManager#submit(String, Submission)}
	 * 
	 * Then it's redirected to $PASTAUrl$/mirror/ to clear the form submission
	 * buffer such that pressing reload has no chance of resubmitting your form.
	 * 
	 * @param form
	 *            the submission form holding the code
	 * @param result
	 *            the binding result used for feedback
	 * @param model
	 *            the model being used
	 * @param session
	 *            the http session used for allowing passing the binding result
	 *            along while using $PASTAUrl$/mirror/ to redirect and such that
	 *            you will not resubmit the form if you refresh the page
	 * @return "redirect:/login/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "student/{studentUsername}/home/", method = RequestMethod.POST)
	public String submitAssessment(@ModelAttribute("user") PASTAUser user, @PathVariable("studentUsername") String username,
			@ModelAttribute(value = "submission") Submission form, BindingResult result, Model model,
			RedirectAttributes attr, HttpSession session) {
		if(username == null || username.isEmpty()) {
			return "redirect:/home/";
		}
		return doSubmitAssessment(user, username, form, result, model, attr, session);
	}
}
