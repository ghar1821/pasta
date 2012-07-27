package info1103.project.web.controller;

import info1103.project.domain.AllStudentAssessmentData;
import info1103.project.domain.LoginForm;
import info1103.project.domain.Submission;
import info1103.project.login.AuthValidator;
import info1103.project.service.SubmissionManager;
import info1103.project.view.ExcelMarkView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.servlet.ModelAndView;

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
	
	@ModelAttribute("submission")
	public Submission returnModel(){
		return new Submission();
	}
	
	/**
	 * Get the currently logged in user.
	 * @return
	 */
	public String getUser(){
		return (String) RequestContextHolder.currentRequestAttributes().getAttribute( "user", RequestAttributes.SCOPE_SESSION);
	}
	
	// history
	@RequestMapping(value="home/submission/{taskname}", method=RequestMethod.GET)
	public String history(@PathVariable("taskname") String taskname, Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		
		model.addAttribute("latestSubmission", manager.getAssessment(getUser(), taskname));
		model.addAttribute("submissionHistory", manager.getAssessmentHistory(getUser(), taskname));
		return ("user/assessment");
	}
	
	// Battleship League TODO
	@RequestMapping(value="home/submission/BattleshipLeague", method=RequestMethod.GET)
	public String battleshipLeague(Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		
		return ("user/TODO");
	}
	
	// home
	@RequestMapping(value="home")
	public String home(Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		
		model.addAttribute("user", manager.getUser(getUser()));
		model.addAttribute("allAssessments", manager.getAssessments(getUser()));
		model.addAttribute("assessmentList", manager.getAssessmentList());
		
		return "user/index";
	}
	
	// home
	@RequestMapping(value="{unikey}")
	public String view(@PathVariable("unikey") String unikey, Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		if(!manager.getUser(getUser()).isTutor()){
			return null;
		}
		
		model.addAttribute("user", manager.getUser(unikey));
		model.addAttribute("allAssessments", manager.getAssessments(unikey));
		model.addAttribute("assessmentList", manager.getAssessmentList());
		
		return "user/index";
	}
	
	// home
	@RequestMapping(value="{unikey}/submission/{taskname}")
	public String view(@PathVariable("unikey") String unikey, @PathVariable("taskname") String taskname, Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		if(!manager.getUser(getUser()).isTutor()){
			return null;
		}
		
		model.addAttribute("latestSubmission", manager.getAssessment(unikey, taskname));
		model.addAttribute("submissionHistory", manager.getAssessmentHistory(unikey, taskname));
		return ("user/assessment");
	}
	
	// all students
	@RequestMapping(value="all")
	public String view(Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		if(!manager.getUser(getUser()).isTutor()){
			return null;
		}
		
		model.addAttribute("allStudents", AllStudentAssessmentData.getInstance().getData());
		model.addAttribute("allAssessments", manager.getAssessmentList());
		return "user/viewAll";
	}
	
	// excel file
	@RequestMapping(value="all.xls")
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if(getUser() == null){
			return null;
		}
		if(!manager.getUser(getUser()).isTutor()){
			return null;
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("allStudents", AllStudentAssessmentData.getInstance().getData());
		data.put("allAssessments", manager.getAssessmentList());
		
		return new ModelAndView(new ExcelMarkView(), data);
	}
	
	@RequestMapping(value="home", method=RequestMethod.POST)
	// after submission of an assessment
	public String home(@ModelAttribute(value="submission") Submission form ,BindingResult result, Model model){
		if(getUser() == null){
			return "user/notloggedin";
		}
		
		
		if(!manager.getAssessment(getUser(), form.getAssessmentName()).isPastDueDate() || manager.getUser(getUser()).isTutor()){
			manager.validateSubmission(form, result);
		}
		else{
			result.reject("Submission.PastDueDate");
		}
		
		model.addAttribute("user", manager.getUser(getUser()));
		model.addAttribute("allAssessments", manager.getAssessments(getUser()));
		model.addAttribute("assessmentList", manager.getAssessmentList());
		
		return "user/index";
	}
	
	@RequestMapping(value="login", method = RequestMethod.GET)  
    public String get(ModelMap model) {  
        model.addAttribute("LOGINFORM", new LoginForm());
        // Because we're not specifying a logical view name, the  
        // DispatcherServlet's DefaultRequestToViewNameTranslator kicks in.  
        return "login";  
    }  
	
	@RequestMapping(value="login", method = RequestMethod.POST)  
    public String index(@ModelAttribute(value="LOGINFORM") LoginForm userMsg,  
            BindingResult result) {  
          
        validator.validate(userMsg, result);  
        if (result.hasErrors()) { 
        	return "login"; 
        }  
          
        RequestContextHolder.currentRequestAttributes().setAttribute("user", userMsg.getUnikey(), RequestAttributes.SCOPE_SESSION);
        // Use the redirect-after-post pattern to reduce double-submits.  
        return "redirect:/home";  
    } 
	
	@RequestMapping("login/exit")
	public String logout() {
		RequestContextHolder.currentRequestAttributes().removeAttribute("user", RequestAttributes.SCOPE_SESSION);
		return "redirect:../";
	}
}
