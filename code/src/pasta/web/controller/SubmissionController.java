package pasta.web.controller;


import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.LoginForm;
import pasta.domain.template.UnitTest;
import pasta.domain.upload.NewUnitTest;
import pasta.domain.upload.Submission;
import pasta.login.AuthValidator;
import pasta.service.SubmissionManager;

@Controller
@RequestMapping("/")
/**
 * Controller class. 
 * 
 * Handles mappings of a url to a method.
 * 
 * @author Alex
 *
 */
public class SubmissionController {
	protected final Log logger = LogFactory.getLog(getClass());

	private SubmissionManager manager;
	private AuthValidator validator = new AuthValidator();

	@Autowired
	public void setMyService(SubmissionManager myService) {
		this.manager = myService;
	}

	@ModelAttribute("newUnitTestModel")
	public NewUnitTest returnNewUnitTestModel() {
		return new NewUnitTest();
	}
	
	@ModelAttribute("submission")
	public Submission returnNewSubmissionModel() {
		return new Submission();
	}

	/**
	 * Get the currently logged in user.
	 * 
	 * @return
	 */
	public String getUser() {
		return (String) RequestContextHolder.currentRequestAttributes().getAttribute("user",
				RequestAttributes.SCOPE_SESSION);
	}

//	// history
//	@RequestMapping(value = "home/submission/{taskname}", method = RequestMethod.GET)
//	public String history(@PathVariable("taskname") String taskname, Model model) {
//		if (getUser() == null) {
//			return "user/notloggedin";
//		}
//
//		model.addAttribute("user", manager.getUser(getUser()));
//		model.addAttribute("unikey", getUser());
//		model.addAttribute("latestSubmission", manager.getAssessment(getUser(), taskname));
//		model.addAttribute("submissionHistory", manager.getAssessmentHistory(getUser(), taskname));
//		return ("user/assessment");
//	}
//
//	// home
//	@RequestMapping(value = "home")
//	public String home(Model model) {
//		if (getUser() == null) {
//			return "user/notloggedin";
//		}
//
//		model.addAttribute("unikey", getUser());
//		model.addAttribute("user", manager.getUser(getUser()));
//		model.addAttribute("allAssessments", manager.getAssessments(getUser()));
//		model.addAttribute("assessmentList", manager.getAssessmentList());
//
//		return "user/index";
//	}
//
//	// home
//	@RequestMapping(value = "{unikey}")
//	public String view(@PathVariable("unikey") String unikey, Model model) {
//		if (getUser() == null) {
//			return "user/notloggedin";
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return null;
//		}
//
//		model.addAttribute("user", manager.getUser(getUser()));
//		model.addAttribute("unikey", unikey);
//		model.addAttribute("allAssessments", manager.getAssessments(unikey));
//		model.addAttribute("assessmentList", manager.getAssessmentList());
//
//		return "user/index";
//	}
//
//	// home
//	@RequestMapping(value = "{unikey}/submission/{taskname}")
//	public String view(@PathVariable("unikey") String unikey, @PathVariable("taskname") String taskname, Model model) {
//		if (getUser() == null) {
//			return "user/notloggedin";
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return null;
//		}
//
//		model.addAttribute("user", manager.getUser(getUser()));
//		model.addAttribute("unikey", unikey);
//		model.addAttribute("latestSubmission", manager.getAssessment(unikey, taskname));
//		model.addAttribute("submissionHistory", manager.getAssessmentHistory(unikey, taskname));
//		return ("user/assessment");
//	}
//
//	// download 1 student 1 task
//	@RequestMapping(value = "download/{unikey}-{taskname}", method = RequestMethod.GET)
//	public void download(@PathVariable("unikey") String unikey, @PathVariable("taskname") String taskname,
//			HttpServletResponse response) {
//		if (getUser() == null) {
//			return;
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return;
//		}
//		try {
//			InputStream file = new FileInputStream(ProjectProperties.getInstance().getSubmissionsLocation() + "/"
//					+ unikey + "/" + taskname + "/latest/" + (unikey + "-" + taskname.toLowerCase().replace(" ", ""))
//					+ ".zip");
//			response.setContentType("application/zip");
//	        response.setHeader( "Content-Disposition", "attachment; filename=" + unikey + "-" + taskname.toLowerCase().replace(" ", "")+ ".zip" );
//			IOUtils.copy(file, response.getOutputStream());
//			response.flushBuffer();
//		} catch (IOException e) {
//			logger.info("Error downloading file. "
//					+ (ProjectProperties.getInstance().getSubmissionsLocation() + "/" + unikey + "/" + taskname
//							+ "/latest/" + (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip"));
//		}
//
//	}
//
//	// download 1 student TODO
//	@RequestMapping(value = "downloadall/{unikey}", method = RequestMethod.GET)
//	public void download(@PathVariable("unikey") String unikey, HttpServletResponse response) {
//		if (getUser() == null) {
//			return;
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return;
//		}
//		try {
//			byte[] buf = new byte[1024];
//
//			try {
//				// Create the ZIP file
//				String outFilename = ProjectProperties.getInstance().getSubmissionsLocation() + "/" + unikey + "/"
//						+ unikey + ".zip";
//				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
//
//				Map<String, Assessment2> data = manager.getAssessments(unikey);
//
//				for (String taskname : data.keySet()) {
//					try {
//						FileInputStream in = new FileInputStream(ProjectProperties.getInstance()
//								.getSubmissionsLocation()
//								+ "/"
//								+ unikey
//								+ "/"
//								+ taskname
//								+ "/latest/"
//								+ (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip");
//
//						// Add ZIP entry to output stream.
//						out.putNextEntry(new ZipEntry(unikey + "/"
//								+ (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip"));
//
//						// Transfer bytes from the file to the ZIP file
//						int len;
//						while ((len = in.read(buf)) > 0) {
//							out.write(buf, 0, len);
//						}
//
//						// Complete the entry
//						out.closeEntry();
//						in.close();
//					} catch (FileNotFoundException e) {
//						// do nothing
//					}
//				}
//				// Complete the ZIP file
//				out.close();
//			} catch (IOException e) {
//			}
//
//			InputStream file = new FileInputStream(ProjectProperties.getInstance().getSubmissionsLocation() + "/"
//					+ unikey + "/" + unikey + ".zip");
//			response.setContentType("application/zip");
//	        response.setHeader( "Content-Disposition", "attachment; filename=" + unikey + ".zip" );
//			IOUtils.copy(file, response.getOutputStream());
//			response.flushBuffer();
//
//			File zipFile = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/" + unikey + "/"
//					+ unikey + ".zip");
//			zipFile.delete();
//		} catch (IOException e) {
//			logger.info("Error downloading file. "
//					+ (ProjectProperties.getInstance().getSubmissionsLocation() + "/" + unikey + "/" + unikey + ".zip"));
//		}
//
//	}
//
//	// download all students in a class
//	@RequestMapping(value = "downloadClass/{tutorialClass}", method = RequestMethod.GET)
//	public void downloadclass(@PathVariable("tutorialClass") String tutorialClass, HttpServletResponse response) {
//		if (getUser() == null) {
//			return;
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return;
//		}
//		try {
//			byte[] buf = new byte[1024];
//			String outFilename = ProjectProperties.getInstance().getSubmissionsLocation() + "/"+tutorialClass+".zip";
//			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
//
//			for (String unikey : manager.getUserList()) {
//				if (manager.getUser(unikey).getTutorialClass() != null
//						&& manager.getUser(unikey).getTutorialClass().equalsIgnoreCase(tutorialClass)) {
//					try {
//						// Create the ZIP file
//
//						Map<String, Assessment2> data = manager.getAssessments(unikey);
//
//						for (String taskname : data.keySet()) {
//							try {
//								FileInputStream in = new FileInputStream(ProjectProperties.getInstance()
//										.getSubmissionsLocation()
//										+ "/"
//										+ unikey
//										+ "/"
//										+ taskname
//										+ "/latest/"
//										+ (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip");
//
//								// Add ZIP entry to output stream.
//								out.putNextEntry(new ZipEntry(unikey + "/"
//										+ (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip"));
//
//								// Transfer bytes from the file to the ZIP file
//								int len;
//								while ((len = in.read(buf)) > 0) {
//									out.write(buf, 0, len);
//								}
//
//								// Complete the entry
//								out.closeEntry();
//								in.close();
//							} catch (FileNotFoundException e) {
//								// do nothing
//							}
//						}
//
//					} catch (IOException e) {
//					}
//
//				}
//			}
//			// Complete the ZIP file
//			out.close();
//
//			InputStream file = new FileInputStream(ProjectProperties.getInstance().getSubmissionsLocation()
//					+ "/"+tutorialClass+".zip");
//			response.setContentType("application/zip");
//	        response.setHeader( "Content-Disposition", "attachment; filename=" + tutorialClass + ".zip" );
//			IOUtils.copy(file, response.getOutputStream());
//			response.flushBuffer();
//
//			File zipFile = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/"+tutorialClass+".zip");
//			zipFile.delete();
//		} catch (IOException e) {
//			logger.info("Error downloading file. "
//					+ (ProjectProperties.getInstance().getSubmissionsLocation() + "/"+tutorialClass+".zip"));
//		}
//
//	}
//
//	// download all students
//	@RequestMapping(value = "downloadall", method = RequestMethod.GET)
//	public void download(HttpServletResponse response) {
//		if (getUser() == null) {
//			return;
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return;
//		}
//		try {
//
//			byte[] buf = new byte[1024];
//			String outFilename = ProjectProperties.getInstance().getSubmissionsLocation() + "/all.zip";
//			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
//
//			for (String unikey : manager.getUserList()) {
//				logger.info(unikey + " - " + manager.getUserList().length);
//				try {
//					// Create the ZIP file
//
//					Map<String, Assessment2> data = manager.getAssessments(unikey);
//
//					for (String taskname : data.keySet()) {
//						try {
//							logger.info(unikey + " - " + taskname);
//							FileInputStream in = new FileInputStream(ProjectProperties.getInstance()
//									.getSubmissionsLocation()
//									+ "/"
//									+ unikey
//									+ "/"
//									+ taskname
//									+ "/latest/"
//									+ (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip");
//
//							// Add ZIP entry to output stream.
//							out.putNextEntry(new ZipEntry(unikey + "/"
//									+ (unikey + "-" + taskname.toLowerCase().replace(" ", "")) + ".zip"));
//
//							// Transfer bytes from the file to the ZIP file
//							int len;
//							while ((len = in.read(buf)) > 0) {
//								out.write(buf, 0, len);
//							}
//
//							// Complete the entry
//							out.closeEntry();
//							in.close();
//						} catch (FileNotFoundException e) {
//							// do nothing
//							logger.info(e.getMessage());
//						}
//					}
//					// Complete the ZIP file
//
//				} catch (IOException e) {
//				}
//
//			}
//			out.close();
//
//			InputStream file = new FileInputStream(ProjectProperties.getInstance().getSubmissionsLocation()
//					+ "/all.zip");
//			response.setContentType("application/zip");
//	        response.setHeader( "Content-Disposition", "attachment; filename=downloadall.zip" );
//			IOUtils.copy(file, response.getOutputStream());
//			response.flushBuffer();
//
//		} catch (IOException e) {
//			logger.info("Error downloading file. "
//					+ (ProjectProperties.getInstance().getSubmissionsLocation() + "/all.zip"));
//		}
//
//		try {
//			File zipFile = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/all.zip");
//			zipFile.delete();
//		} catch (Exception e) {
//			// do nothing
//		}
//	}
//
//	// all students
//	@RequestMapping(value = "all")
//	public String view(Model model) {
//		if (getUser() == null) {
//			return "user/notloggedin";
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return null;
//		}
//
//		model.addAttribute("allStudents", AllStudentAssessmentData.getInstance().getData());
//		model.addAttribute("allAssessments", manager.getAssessmentList());
//		return "user/viewAll";
//	}
//
//	// excel file
//	@RequestMapping(value = "all.xls")
//	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		if (getUser() == null) {
//			return null;
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return null;
//		}
//
//		Map<String, Object> data = new HashMap<String, Object>();
//
//		data.put("allStudents", AllStudentAssessmentData.getInstance().getData());
//		data.put("allAssessments", manager.getAssessmentList());
//
//		return new ModelAndView(new ExcelMarkView(), data);
//	}
//
//	@RequestMapping(value = "home", method = RequestMethod.POST)
//	// after submission of an assessment
//	public String home(@ModelAttribute(value = "submission") Submission form, BindingResult result, Model model) {
//		if (getUser() == null) {
//			return "user/notloggedin";
//		}
//
//		if (!manager.getAssessment(getUser(), form.getAssessmentName()).isPastDueDate()
//				|| manager.getUser(getUser()).isTutor()) {
//			manager.validateSubmission(form, result);
//		} else {
//			result.reject("Submission.PastDueDate");
//		}
//
////		model.addAttribute("user", manager.getUser(getUser()));
////		model.addAttribute("allAssessments", manager.getAssessments(getUser()));
////		model.addAttribute("assessmentList", manager.getAssessmentList());
//
//		return "redirect:/home";
//	}
//
//
//	@RequestMapping("reloadCache")
//	public String reload() {
//		if (getUser() == null) {
//			return null;
//		}
//		if (!manager.getUser(getUser()).isTutor()) {
//			return null;
//		}
//
//		AllStudentAssessmentData.getInstance().reload();
//		return "redirect:home";
//	}
	
	// NEW
	
	// view an assessment
	@RequestMapping(value = "assessments/view/{assessmentName}/")
	public String viewAssessment(@PathVariable("assessmentName") String assessmentName, Model model) {

		model.addAttribute("assessment", manager.getAssessmentNew(assessmentName));
		return "assessment/view/assessment";
	}
	
	// view an assessment
	@RequestMapping(value = "assessments/viewAll/")
	public String viewAllAssessment(Model model) {

		model.addAttribute("allAssessments", manager.getAssessmentListNew());
		return "assessment/viewAll/assessment";
	}
	
	// view a unit test
	@RequestMapping(value = "unitTest/view/{testName}/")
	public String viewUnitTest(@PathVariable("testName") String testName, Model model) {

		model.addAttribute("unitTest", manager.getUnitTest(testName));
		return "assessment/view/unitTest";
	}
	
	// view a unit test
	@RequestMapping(value = "unitTest/view/{testName}/", method = RequestMethod.POST)
	public String uploadTestCode(@PathVariable("testName") String testName, @ModelAttribute(value = "submission") Submission form, 
			BindingResult result,  Model model) {

		// if submission exists
		if(form.getFile() != null && !form.getFile().isEmpty()){
			// upload submission
			manager.testUnitTest(form, testName);
		}
		
		return "redirect:.";
	}
	
	// view all unit tests
	@RequestMapping(value = "unitTest/viewAll/")
	public String viewUnitTest(Model model) {

		model.addAttribute("allUnitTests", manager.getUnitTestList());
		return "assessment/viewAll/unitTest";
	}
	
	// delete a unit test
	@RequestMapping(value = "unitTest/delete/{testName}/")
	public String deleteUnitTest(@PathVariable("testName") String testName, Model model) {
		manager.removeUnitTest(testName);
		return "redirect:../../viewAll/";
	}
	
	@RequestMapping(value = "unitTest/viewAll/", method = RequestMethod.POST)
	// after submission of an assessment
	public String home(@ModelAttribute(value = "newUnitTestModel") NewUnitTest form, BindingResult result, Model model) {

		// check if the name is unique
		Collection<UnitTest> allUnitTests = manager.getUnitTestList();
		
		for(UnitTest test: allUnitTests){
			if(test.getName().toLowerCase().replace(" ", "").equals(form.getTestName().toLowerCase().replace(" ", ""))){
				result.reject("UnitTest.New.NameNotUnique");
			}
		}
		
		// add it.
		if(!result.hasErrors()){
			manager.addUnitTest(form);
		}

		return "redirect:.";
	}
	
	// modify an assessment
	@RequestMapping(value = "assessments/modify/{assessmentName}/")
	public String modifyAssessment(@PathVariable("assessmentName") String assessmentName, Model model) {
		
		
		return "assessment/modify/assessment";
	}
	
	// home page
	@RequestMapping(value = "home/")
	public String home(Model model) {
		// check if tutor or student TODO
		return "user/studentHome";
	}
	
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String get(ModelMap model) {
		model.addAttribute("LOGINFORM", new LoginForm());
		// Because we're not specifying a logical view name, the
		// DispatcherServlet's DefaultRequestToViewNameTranslator kicks in.
		return "login";
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	public String index(@ModelAttribute(value = "LOGINFORM") LoginForm userMsg, BindingResult result) {

		validator.validate(userMsg, result);
		if (result.hasErrors()) {
			return "login";
		}

		RequestContextHolder.currentRequestAttributes().setAttribute("user", userMsg.getUnikey(),
				RequestAttributes.SCOPE_SESSION);
		// Use the redirect-after-post pattern to reduce double-submits.
		return "redirect:/home/";
	}

	@RequestMapping("login/exit")
	public String logout() {
		RequestContextHolder.currentRequestAttributes().removeAttribute("user", RequestAttributes.SCOPE_SESSION);
		return "redirect:../";
	}
	
}