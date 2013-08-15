package pasta.web.controller;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

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
import pasta.domain.ReleaseForm;
import pasta.domain.result.AssessmentResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.service.SubmissionManager;
import pasta.util.ProjectProperties;

@Controller
@RequestMapping("assessments/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class AssessmentsController {

	public AssessmentsController() {
		codeStyle = new HashMap<String, String>();
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

	private SubmissionManager manager;
	private HashMap<String, String> codeStyle;

	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.manager = myService;
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
	 * Get the currently logged in user.
	 * 
	 * @return
	 */
	public PASTAUser getOrCreateUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return manager.getOrCreateUser(username);
		}
		return null;
	}

	public PASTAUser getUser() {
		String username = (String) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		if (username != null) {
			return manager.getUser(username);
		}
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ASSESSMENTS //
	// ///////////////////////////////////////////////////////////////////////////

	// view an assessment
	@RequestMapping(value = "{assessmentName}/", method = RequestMethod.POST)
	public String viewAssessment(
			@PathVariable("assessmentName") String assessmentName,
			@ModelAttribute(value = "assessment") Assessment form,
			BindingResult result, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (user.isInstructor()) {
			form.setName(manager.getAssessment(assessmentName).getName());
			manager.addAssessment(form);
		}
		return "redirect:.";
	}

	// view an assessment
	@RequestMapping(value = "{assessmentName}/run/")
	public String runAssessment(
			@PathVariable("assessmentName") String assessmentName,
			HttpServletRequest request) {

		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (user.isInstructor()) {
			manager.runAssessment(manager.getAssessment(assessmentName));
		}
		String referer = request.getHeader("Referer");
		return "redirect:" + referer;
	}

	// view an assessment
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

		Assessment currAssessment = manager.getAssessment(assessmentName);
		model.addAttribute("assessment", currAssessment);

		List<WeightedUnitTest> otherUnitTetsts = new LinkedList<WeightedUnitTest>();

		for (UnitTest test : manager.getUnitTestList()) {
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

		for (HandMarking test : manager.getHandMarkingList()) {
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

		for (Competition test : manager.getCompetitionList()) {
			boolean contains = false;
			for (WeightedCompetition weightedComp : currAssessment
					.getCompetitions()) {
				if (weightedComp.getTest() == test) {
					contains = true;
					break;
				}
			}

			if (!contains) {
				WeightedCompetition weigthedComp = new WeightedCompetition();
				weigthedComp.setTest(test);
				weigthedComp.setWeight(0);
				otherCompetitions.add(weigthedComp);
			}
		}

		model.addAttribute("tutorialByStream", manager.getTutorialByStream());
		model.addAttribute("otherUnitTests", otherUnitTetsts);
		model.addAttribute("otherHandMarking", otherHandMarking);
		model.addAttribute("otherCompetitions", otherCompetitions);
		model.addAttribute("unikey", user);
		return "assessment/view/assessment";
	}

	// view an assessment
	@RequestMapping(value = "")
	public String viewAllAssessment(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		model.addAttribute("tutorialByStream", manager.getTutorialByStream());
		model.addAttribute("allAssessments", manager.getAssessmentList());
		model.addAttribute("unikey", user);
		return "assessment/viewAll/assessment";
	}

	// view an assessment
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
				manager.addAssessment(form);
			}
		}
		return "redirect:.";
	}

	// release an assessment
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
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {

			if (manager.getAssessment(assessmentName) != null) {
				manager.releaseAssessment(form.getAssessmentName(),
						form);
			}
		}
		return "redirect:../../";
	}

	// delete an assessment
	@RequestMapping(value = "delete/{assessmentName}/")
	public String deleteAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		if (getUser().isInstructor()) {
			manager.removeAssessment(assessmentName);
		}
		return "redirect:../../";
	}

	// stats of an assessment
	@RequestMapping(value = "stats/{assessmentName}/")
	public String statisticsForAssessment(
			@PathVariable("assessmentName") String assessmentName, Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/.";
		}
		HashMap<String, HashMap<String, AssessmentResult>> allResults = manager
				.getLatestResults();
		TreeMap<Integer, Integer> submissionDistribution = new TreeMap<Integer, Integer>();

		int maxBreaks = 10;

		int[] markDistribution = new int[maxBreaks + 1];

		for (Entry<String, HashMap<String, AssessmentResult>> entry : allResults
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
				manager.getAssessment(assessmentName));
		model.addAttribute("maxBreaks", maxBreaks);
		model.addAttribute("markDistribution", markDistribution);
		model.addAttribute("submissionDistribution", submissionDistribution);
		model.addAttribute("unikey", user);
		return "assessment/view/assessmentStats";
	}

}