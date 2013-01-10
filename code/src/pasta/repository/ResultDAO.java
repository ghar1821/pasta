package pasta.repository;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.ProjectProperties;

public class ResultDAO {
	
	protected final Log logger = LogFactory.getLog(getClass());
	// username, assessment, date
	HashMap<String, HashMap<String, HashMap<String, AssessmentResult>>> results;
	
	public ResultDAO(AssessmentDAO assDao){
		loadAssessmentHistoryFromFile(assDao);
	}

	public UnitTestResult getUnitTestResult(String location){
		UnitTestResult result = new UnitTestResult();
		
		// check to see if there is a results.xml file
		File testResults = new File(location+"/result.xml");
		if(testResults.exists()){
			try{	
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(testResults);
				doc.getDocumentElement().normalize();
				
				ArrayList<UnitTestCaseResult> testCases = new ArrayList<UnitTestCaseResult>();
				
				NodeList unitTestList = doc.getElementsByTagName("testcase");
				if (unitTestList != null && unitTestList.getLength() > 0) {
					for (int i = 0; i < unitTestList.getLength(); i++) {
						Node unitTestNode = unitTestList.item(i);
						if (unitTestNode.getNodeType() == Node.ELEMENT_NODE) {
							UnitTestCaseResult caseResult = new UnitTestCaseResult();
							
							Element unitTestElement = (Element) unitTestNode;
							// name
							caseResult.setTestName(unitTestElement.getAttribute("name"));
							// time
							caseResult.setTime(Double.parseDouble(unitTestElement.getAttribute("time")));
							// test case - assume it is a pass.
							caseResult.setTestResult("pass");
							
							// if failed
							if(unitTestElement.hasChildNodes()){
								Element failedUnitTestElement = (Element) unitTestNode.getChildNodes().item(1);
								
								// new result
								caseResult.setTestResult(failedUnitTestElement.getNodeName());
								// message
								if(failedUnitTestElement.hasAttribute("message")){
									caseResult.setTestMessage(failedUnitTestElement.getAttribute("message"));
								}
								// type
								if(failedUnitTestElement.hasAttribute("type")){
									caseResult.setType((failedUnitTestElement.getAttribute("type")));
								}
								// extended message
								if(failedUnitTestElement.getTextContent() != null){
									caseResult.setExtendedMessage(failedUnitTestElement.getTextContent());
								}
							}
							testCases.add(caseResult);
						}
					}
				}
				result.setTestCases(testCases);
				return result;
			} 
			catch (Exception e){
				logger.error("Could not read result.xml" + System.getProperty("line.separator")+e);
			}
		}
		
		// check to see if there is a compile.errors file
		File compileErrors = new File(location+"/compile.errors");
		System.out.println(location+"/compile.errors");
		if(compileErrors.exists()){
			System.out.println("exists");
			try{
				// read in
				Scanner in = new Scanner (compileErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine() + System.getProperty("line.separator");
				}
				// set 
				result.setCompileErrors(input);
				
				// return
				return result;
			}
			catch(Exception e){
				logger.error("Could not read compile.errors" + System.getProperty("line.separator")+e);
			}
		}
		
		// check to see if there is a run.errors file
		File runErrors = new File(location+"/run.errors");
		if(runErrors.exists()){
			try{
				// read in
				Scanner in = new Scanner (runErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine() + System.getProperty("line.separator");
				}
				// set 
				result.setRuntimeErrors(input);
				
				// return
				return result;
			}
			catch(Exception e){
				logger.error("Could not read run.errors" + System.getProperty("line.separator")+e);
			}
		}
		
		return null; // TODO check if in the database
	}
	
	public HashMap<String, AssessmentResult> getLatestResults(String username){
		HashMap<String, AssessmentResult> results = new HashMap<String, AssessmentResult>();
		
		if(this.results.get(username) != null){
			for(Entry<String, HashMap<String, AssessmentResult>> allAssesmentResults :this.results.get(username).entrySet()){
				AssessmentResult latest = null;
				for(AssessmentResult result: allAssesmentResults.getValue().values()){
					// too tired to do this nicely.
					latest = result;
					break;
				}
				if(latest != null){
					results.put(allAssesmentResults.getKey(), latest);
				}
			}
		}
		return results;
	}
	
	public Collection<AssessmentResult> getAssessmentHistory(String username, Assessment assessment){
		return results.get(username).get(assessment.getShortName()).values();
	}
	
	private void loadAssessmentHistoryFromFile(AssessmentDAO assDao){
		results = new HashMap<String, HashMap<String, HashMap<String, AssessmentResult>>>();
		
		// scan all users
		String[] allUsers = (new File(ProjectProperties.getInstance().getProjectLocation()+"/submissions/")).list();
		for(String currUser: allUsers){
			// scan all assessments
			Collection<Assessment> allAssessments = assDao.getAssessmentList();
			
			HashMap<String, HashMap<String, AssessmentResult>>currUserResults = new HashMap<String, HashMap<String, AssessmentResult>>();
			for(Assessment assessment: allAssessments){
				// scan all submissions
				
				HashMap<String, AssessmentResult> currAssessmentResults = new HashMap<String, AssessmentResult>();
				String[] allFiles = (new File(ProjectProperties.getInstance()
						.getProjectLocation()
						+ "/submissions/"
						+ currUser
						+ "/assessments/" + assessment.getShortName())).list();
				
				if(allFiles != null){
					Arrays.sort(allFiles);

					for(int i=0; i< allFiles.length; ++i){
						String latest = allFiles[allFiles.length-1-i];
						AssessmentResult assessResult = new AssessmentResult();
						assessResult.setAssessment(assessment);
				
						ArrayList<UnitTestResult> utresults = new ArrayList<UnitTestResult>();
						for (WeightedUnitTest uTest : assessment.getUnitTests()) {
							UnitTestResult result = getUnitTestResult(ProjectProperties.getInstance()
											.getProjectLocation()
											+ "/submissions/"
											+ currUser
											+ "/assessments/"
											+ assessment.getShortName()
											+ "/"
											+ latest
											+ "/unitTests/" + uTest.getTest().getShortName());
							if (result == null) {
								result = new UnitTestResult();
							}
							result.setTest(uTest.getTest());
							utresults.add(result);
							
						}
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
						try {
							assessResult.setSubmissionDate(sdf.parse(latest));
						} catch (ParseException e) {
							assessResult.setSubmissionDate(new Date());
							logger.error("Submission date " + latest + " - " + currUser + " - " + assessment.getShortName());
						}
						assessResult.setUnitTests(utresults);
						
						currAssessmentResults.put(latest, assessResult);
					}
				}
				currUserResults.put(assessment.getShortName(), currAssessmentResults);
			}
			results.put(currUser, currUserResults);
		}
	}

	public void loadNewAssessment(String currUser, Assessment assessment,
			Date runDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		String latest = sdf.format(runDate);
		AssessmentResult assessResult = new AssessmentResult();
		assessResult.setAssessment(assessment);

		ArrayList<UnitTestResult> utresults = new ArrayList<UnitTestResult>();
		for (WeightedUnitTest uTest : assessment.getUnitTests()) {
			UnitTestResult result = getUnitTestResult(ProjectProperties.getInstance()
							.getProjectLocation()
							+ "/submissions/"
							+ currUser
							+ "/assessments/"
							+ assessment.getShortName()
							+ "/"
							+ latest
							+ "/unitTests/" + uTest.getTest().getShortName());
			if (result == null) {
				result = new UnitTestResult();
			}
			result.setTest(uTest.getTest());
			utresults.add(result);
			
		}
		try {
			assessResult.setSubmissionDate(sdf.parse(latest));
		} catch (ParseException e) {
			assessResult.setSubmissionDate(new Date());
			logger.error("Submission date " + latest + " - " + currUser + " - " + assessment.getName());
		}
		assessResult.setUnitTests(utresults);
		
		results.get(currUser).get(assessment.getShortName()).put(latest, assessResult);
	}
}
