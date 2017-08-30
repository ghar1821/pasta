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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pasta.domain.UserPermissionLevel;
import pasta.domain.reporting.Report;
import pasta.domain.template.Assessment;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.ReportingManager;
import pasta.service.UserManager;
import pasta.service.reporting.AssessmentReportingManager;
import pasta.service.reporting.UnitTestReportingManager;
import pasta.web.WebUtils;

/**
 * Controller class for reporting functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/reporting/...
 * <p>
 * Both students and teaching staff can access this url.
 * 
 * @author Josh Stretton
 * @version 1.0
 * @since 2017-06-02
 *
 */
@Controller
@RequestMapping("reporting/")
public class ReportingController {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private ReportingManager reportingManager;
	
	@Autowired
	private AssessmentManager assessmentManager;
	
	@Autowired
	private AssessmentReportingManager assessmentReportManager;
	
	@Autowired
	private UnitTestReportingManager unitTestReportManager;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	/**
	 * $PASTAUrl$/reporting/
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the login screen.
	 * <p>
	 * JSP:
	 * <ul>
	 * 	<li>report/reporting</li>
	 * </ul>
	 * 
	 * @param model the model
	 * @return "redirect:/login" or "user/admin".
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String viewReports(@ModelAttribute("user") PASTAUser user, ModelMap model) {
		return viewReportPage(user, model);
	}
	
	@RequestMapping(value = "user/{otherUser}/", method = RequestMethod.GET)
	public String viewReportsAsUser(@ModelAttribute("user") PASTAUser user, 
			@PathVariable("otherUser") String otherUsername, 
			ModelMap model) {
		PASTAUser otherUser = userManager.getUser(otherUsername);
		if(user.equals(otherUser)) {
			return "redirect:../../";
		}
		if(otherUser.isInstructor()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
		model.addAttribute("pretending", otherUser);
		return viewReportPage(otherUser, model);
	}
	
	private String viewReportPage(PASTAUser asUser, ModelMap model) {
		List<Report> allReports = reportingManager.getAllReports(asUser);
		Collections.sort(allReports, (a,  b) -> {
			return a.getName().compareToIgnoreCase(b.getName());
		});
		model.addAttribute("allReports", allReports);
		model.addAttribute("allPermissions", UserPermissionLevel.validReportValues());
		return "report/reporting";
	}
	
	@RequestMapping(value = "{reportId}/", method = RequestMethod.GET)
	@ResponseBody
	public String loadReport(@PathVariable("reportId") String reportId, @ModelAttribute("user") PASTAUser user, ModelMap model) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("reportId", reportId);
		
		Report report = reportingManager.getReport(reportId);
		if(reportingManager.userCanViewReport(user, report)) {
			Collection<Assessment> allAssessments = assessmentManager.getReleasedAssessments(user);
			ArrayNode assessmentsNode = mapper.createArrayNode();
			for(Assessment assessment : allAssessments) {
				ObjectNode summaryNode = mapper.createObjectNode();
				ObjectNode assessmentNode = assessmentReportManager.getAssessmentJSON(assessment);
				summaryNode.set("assessment", assessmentNode);
				assessmentsNode.add(summaryNode);
			}
			node.set("assessments", assessmentsNode);
			
			switch(reportId) {
			case "mark-histograms": {
				node.put("callback", "displayHistograms");
				break;
			}
			case "unit-test-attempts": {
				node.put("callback", "displayUnitTestAttempts");
				break;
			}
			case "assessment-ratings": {
				node.put("callback", "displayRatings");
				break;
			}
			case "submissions-timeline": {
				node.put("callback", "displaySubmissions");
				break;
			}
			}
		} else {
			node.put("error", "You are not allowed to view this report.");
		}
		
		return getJSONString(node);
	}
	
	@RequestMapping(value = "user/{otherUser}/{reportId}/", method = RequestMethod.GET)
	@ResponseBody
	public String loadReportAsUser(@PathVariable("reportId") String reportId, 
			@ModelAttribute("user") PASTAUser user, 
			@PathVariable("otherUser") String otherUserName, 
			ModelMap model) {
		PASTAUser otherUser = userManager.getUser(otherUserName);
		if(otherUser.isInstructor()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
		return loadReport(reportId, otherUser, model);
	}
	
	@RequestMapping(value = "{reportId}/{assessmentId}/", method = RequestMethod.GET)
	@ResponseBody
	public String loadReportDetails(@PathVariable("reportId") String reportId, @PathVariable("assessmentId") long assessmentId, @ModelAttribute("user") PASTAUser user, ModelMap model) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		
		Report report = reportingManager.getReport(reportId);
		if(reportingManager.userCanViewReport(user, report)) {
			Assessment assessment = assessmentManager.getAssessment(assessmentId);
			if(assessment == null) {
				node.put("error", "Assessment ID " + assessmentId + " does not exist");
			} else {
				switch(reportId) {
				case "mark-histograms": {
					node = assessmentReportManager.getMarksSummaryJSON(assessment, user);
					break;
				}
				case "unit-test-attempts": {
					node = unitTestReportManager.getAllTestsSummaryJSON(assessment, user);
					break;
				}
				case "assessment-ratings": {
					node = assessmentReportManager.getAssessmentRatingsJSON(assessment);
					break;
				}
				case "submissions-timeline": {
					node = assessmentReportManager.getAssessmentSubmissionsJSON(assessment);
					break;
				}
				}
				node.set("assessment", assessmentReportManager.getAssessmentJSON(assessment));
			}
		} else {
			node.put("error", "You are not allowed to view this report.");
		}
		
		return getJSONString(node);
	}
	
	@RequestMapping(value = "user/{otherUser}/{reportId}/{assessmentId}/", method = RequestMethod.GET)
	@ResponseBody
	public String loadReportDetailsAsUser(@PathVariable("reportId") String reportId, 
			@PathVariable("assessmentId") long assessmentId, 
			@ModelAttribute("user") PASTAUser user, 
			@PathVariable("otherUser") String otherUserName, 
			ModelMap model) {
		PASTAUser otherUser = userManager.getUser(otherUserName);
		if(otherUser.isInstructor()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
		return loadReportDetails(reportId, assessmentId, otherUser, model);
	}
	
	@RequestMapping(value = "savePermissions/{reportId}/", method = RequestMethod.POST)
	@ResponseBody
	public String savePermissions(@PathVariable("reportId") String reportId, 
			@ModelAttribute("user") PASTAUser user, 
			@RequestParam(value="permissions[]", required=false) List<UserPermissionLevel> permissions,
			ModelMap model) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		
		logger.info("User " + user.getUsername() + " saving report permissions for " + reportId + ": " + permissions);
		Report report = reportingManager.getReport(reportId);
		report.setPermissionLevels(permissions);
		reportingManager.saveOrUpdate(report);
		
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("result", "success");
		node.put("reportId", reportId);
		return getJSONString(node);
	}
	
	private String getJSONString(JsonNode node) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(node);
		} catch (JsonProcessingException e) {
			return "{'error':'parsing'}";
		}
	}
}
